package ru.alexey.coursehub.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.UpdateEnrollmentRequest;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;
import ru.alexey.coursehub.entity.enums.NotificationType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.EnrollmentService;
import ru.alexey.coursehub.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Enrollment enroll(Long courseId, Long studentId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> NotFoundException.of("Course", courseId));
        User student = requireStudent(studentId);
        if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ConflictException("Student " + studentId + " is already enrolled in course " + courseId);
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setStudent(student);
        enrollment.setEnrollDate(LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        Enrollment saved = enrollmentRepository.save(enrollment);
        notificationService.notify(
                studentId, NotificationType.ENROLLMENT, "You have been enrolled in course " + course.getTitle());
        log.info("Student {} enrolled in course {}", studentId, courseId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Enrollment get(Long id) {
        Enrollment enrollment =
                enrollmentRepository.findById(id).orElseThrow(() -> NotFoundException.of("Enrollment", id));
        enrollment.getCourse().getTitle();
        enrollment.getStudent().getName();
        return enrollment;
    }

    @Override
    @Transactional
    public Enrollment updateStatus(Long id, UpdateEnrollmentRequest request) {
        Enrollment enrollment = get(id);
        enrollment.setStatus(request.status());
        log.info("Enrollment {} status set to {}", id, request.status());
        return enrollment;
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Enrollment enrollment = get(id);
        enrollmentRepository.delete(enrollment);
        log.info("Cancelled enrollment {}", id);
    }

    @Override
    @Transactional
    public void cancel(Long courseId, Long studentId) {
        Enrollment enrollment = enrollmentRepository
                .findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(
                        () -> NotFoundException.of("Enrollment for course " + courseId + " and student", studentId));
        enrollmentRepository.delete(enrollment);
        log.info("Cancelled enrollment of student {} in course {}", studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> listByCourse(Long courseId) {
        return enrollmentRepository.findByCourseIdWithStudents(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Enrollment> listByStudent(Long studentId) {
        return enrollmentRepository.findByStudentIdWithCourse(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Enrollment> pageByStudent(Long studentId, EnrollmentStatus status, Pageable pageable) {
        if (status != null) {
            return enrollmentRepository.findByStudentIdAndStatus(studentId, status, pageable);
        }
        return enrollmentRepository.findByStudentId(studentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    private User requireStudent(Long studentId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.of("User", studentId));
        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User " + studentId + " is not a student");
        }
        return user;
    }
}

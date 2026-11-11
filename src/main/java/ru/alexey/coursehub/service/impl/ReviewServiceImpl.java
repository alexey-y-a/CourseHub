package ru.alexey.coursehub.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.ReviewRequest;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.CourseReview;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.CourseReviewRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.ReviewService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final CourseReviewRepository courseReviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public CourseReview add(Long courseId, Long studentId, ReviewRequest request) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> NotFoundException.of("Course", courseId));
        User student = requireStudent(studentId);
        if (!enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new BadRequestException("Student " + studentId + " is not enrolled in course " + courseId);
        }
        if (courseReviewRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
            throw new ConflictException("Student " + studentId + " has already reviewed course " + courseId);
        }
        CourseReview review = new CourseReview();
        review.setCourse(course);
        review.setStudent(student);
        review.setRating(request.rating());
        review.setComment(request.comment());
        CourseReview saved = courseReviewRepository.save(review);
        log.info("Student {} reviewed course {} with rating {}", studentId, courseId, request.rating());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CourseReview> listByCourse(Long courseId, Pageable pageable) {
        return courseReviewRepository.findByCourseId(courseId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Double averageRating(Long courseId) {
        return courseReviewRepository.findAverageRating(courseId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        CourseReview review =
                courseReviewRepository.findById(id).orElseThrow(() -> NotFoundException.of("CourseReview", id));
        courseReviewRepository.delete(review);
        log.info("Deleted review {}", id);
    }

    private User requireStudent(Long studentId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.of("User", studentId));
        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User " + studentId + " is not a student");
        }
        return user;
    }
}

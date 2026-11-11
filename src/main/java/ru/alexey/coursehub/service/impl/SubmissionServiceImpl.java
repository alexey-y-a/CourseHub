package ru.alexey.coursehub.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.GradeSubmissionRequest;
import ru.alexey.coursehub.dto.request.SubmitAssignmentRequest;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Submission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.NotificationType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.entity.enums.SubmissionStatus;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.AssignmentRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.NotificationService;
import ru.alexey.coursehub.service.SubmissionService;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Submission submit(Long assignmentId, Long studentId, SubmitAssignmentRequest request) {
        Assignment assignment = assignmentRepository
                .findByIdWithLesson(assignmentId)
                .orElseThrow(() -> NotFoundException.of("Assignment", assignmentId));
        User student = requireStudent(studentId);
        if (submissionRepository.existsByAssignmentIdAndStudentId(assignmentId, studentId)) {
            throw new ConflictException("Student " + studentId + " has already submitted assignment " + assignmentId);
        }
        Submission submission = new Submission();
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent(request.content());
        submission.setSubmittedAt(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.SUBMITTED);
        Submission saved = submissionRepository.save(submission);
        User teacher = assignment.getLesson().getModule().getCourse().getTeacher();
        notificationService.notify(
                teacher.getId(),
                NotificationType.ASSIGNMENT,
                "Student " + student.getName() + " submitted assignment " + assignment.getTitle());
        log.info("Student {} submitted assignment {}", studentId, assignmentId);
        return saved;
    }

    @Override
    @Transactional
    public Submission grade(Long submissionId, GradeSubmissionRequest request) {
        Submission submission = submissionRepository
                .findById(submissionId)
                .orElseThrow(() -> NotFoundException.of("Submission", submissionId));
        int maxScore = submission.getAssignment().getMaxScore();
        if (request.score() > maxScore) {
            throw new BadRequestException("Score " + request.score() + " exceeds max score " + maxScore);
        }
        submission.setScore(request.score());
        submission.setFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.GRADED);
        notificationService.notify(
                submission.getStudent().getId(),
                NotificationType.GRADE,
                "Submission " + submissionId + " graded with score " + request.score());
        log.info("Graded submission {} with score {}", submissionId, request.score());
        return submission;
    }

    @Override
    @Transactional(readOnly = true)
    public Submission get(Long id) {
        Submission submission =
                submissionRepository.findById(id).orElseThrow(() -> NotFoundException.of("Submission", id));
        submission.getStudent().getName();
        submission.getAssignment().getTitle();
        return submission;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> listByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Submission> pageByStudent(Long studentId, Pageable pageable) {
        return submissionRepository.findByStudentId(studentId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Submission> listByStudentAndCourse(Long studentId, Long courseId) {
        return submissionRepository.findByStudentAndCourse(studentId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double averageScore(Long assignmentId) {
        return submissionRepository.findAverageScore(assignmentId);
    }

    private User requireStudent(Long studentId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.of("User", studentId));
        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User " + studentId + " is not a student");
        }
        return user;
    }
}

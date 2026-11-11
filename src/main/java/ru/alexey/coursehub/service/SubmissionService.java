package ru.alexey.coursehub.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.alexey.coursehub.dto.request.GradeSubmissionRequest;
import ru.alexey.coursehub.dto.request.SubmitAssignmentRequest;
import ru.alexey.coursehub.entity.Submission;

public interface SubmissionService {

    Submission submit(Long assignmentId, Long studentId, SubmitAssignmentRequest request);

    Submission grade(Long submissionId, GradeSubmissionRequest request);

    Submission get(Long id);

    List<Submission> listByAssignment(Long assignmentId);

    Page<Submission> pageByStudent(Long studentId, Pageable pageable);

    List<Submission> listByStudentAndCourse(Long studentId, Long courseId);

    Double averageScore(Long assignmentId);
}

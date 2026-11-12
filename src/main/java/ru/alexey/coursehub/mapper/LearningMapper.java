package ru.alexey.coursehub.mapper;

import ru.alexey.coursehub.dto.response.AssignmentResponse;
import ru.alexey.coursehub.dto.response.EnrollmentResponse;
import ru.alexey.coursehub.dto.response.ReviewResponse;
import ru.alexey.coursehub.dto.response.SubmissionResponse;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.CourseReview;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.Submission;

public final class LearningMapper {

    private LearningMapper() {}

    public static AssignmentResponse toResponse(Assignment assignment) {
        return new AssignmentResponse(
                assignment.getId(),
                assignment.getLesson().getId(),
                assignment.getTitle(),
                assignment.getDescription(),
                assignment.getDueDate(),
                assignment.getMaxScore());
    }

    public static SubmissionResponse toResponse(Submission submission) {
        return new SubmissionResponse(
                submission.getId(),
                submission.getAssignment().getId(),
                submission.getAssignment().getTitle(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getSubmittedAt(),
                submission.getContent(),
                submission.getScore(),
                submission.getFeedback(),
                submission.getStatus());
    }

    public static EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getName(),
                enrollment.getEnrollDate(),
                enrollment.getStatus());
    }

    public static ReviewResponse toResponse(CourseReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getCourse().getId(),
                review.getStudent().getId(),
                review.getStudent().getName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}

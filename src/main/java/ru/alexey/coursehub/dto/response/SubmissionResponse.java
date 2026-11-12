package ru.alexey.coursehub.dto.response;

import java.time.LocalDateTime;
import ru.alexey.coursehub.entity.enums.SubmissionStatus;

public record SubmissionResponse(
        Long id,
        Long assignmentId,
        String assignmentTitle,
        Long studentId,
        String studentName,
        LocalDateTime submittedAt,
        String content,
        Integer score,
        String feedback,
        SubmissionStatus status) {}

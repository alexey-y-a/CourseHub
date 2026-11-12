package ru.alexey.coursehub.dto.response;

import java.time.LocalDateTime;

public record AssignmentResponse(
        Long id, Long lessonId, String title, String description, LocalDateTime dueDate, Integer maxScore) {}

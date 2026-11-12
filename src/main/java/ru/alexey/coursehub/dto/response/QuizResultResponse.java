package ru.alexey.coursehub.dto.response;

import java.time.LocalDateTime;

public record QuizResultResponse(
        Long id,
        Long quizId,
        String quizTitle,
        Long studentId,
        String studentName,
        Integer score,
        LocalDateTime takenAt) {}

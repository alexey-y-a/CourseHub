package ru.alexey.coursehub.dto.response;

import java.util.List;

public record QuizResponse(Long id, Long moduleId, String title, Integer timeLimit, List<QuestionResponse> questions) {}

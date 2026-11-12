package ru.alexey.coursehub.dto.response;

public record LessonResponse(Long id, Long moduleId, String title, String content, String videoUrl) {}

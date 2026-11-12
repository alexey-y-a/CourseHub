package ru.alexey.coursehub.dto.response;

public record ModuleResponse(Long id, Long courseId, String title, Integer orderIndex, String description) {}

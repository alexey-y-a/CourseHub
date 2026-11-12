package ru.alexey.coursehub.dto.response;

import java.util.List;

public record ModuleStructureResponse(
        Long id, String title, Integer orderIndex, String description, List<LessonStructureResponse> lessons) {}

package ru.alexey.coursehub.dto.response;

import java.util.List;

public record CourseStructureResponse(Long id, String title, List<ModuleStructureResponse> modules) {}

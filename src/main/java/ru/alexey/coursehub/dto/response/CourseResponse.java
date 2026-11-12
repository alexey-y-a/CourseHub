package ru.alexey.coursehub.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

public record CourseResponse(
        Long id,
        String title,
        String description,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName,
        Integer duration,
        LocalDate startDate,
        Set<String> tags,
        LocalDateTime createdAt) {}

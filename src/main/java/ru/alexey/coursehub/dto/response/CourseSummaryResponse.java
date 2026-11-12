package ru.alexey.coursehub.dto.response;

import java.time.LocalDate;

public record CourseSummaryResponse(
        Long id,
        String title,
        Long categoryId,
        String categoryName,
        Long teacherId,
        String teacherName,
        Integer duration,
        LocalDate startDate) {}

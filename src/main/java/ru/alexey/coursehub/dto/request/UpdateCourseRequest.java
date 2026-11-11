package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

public record UpdateCourseRequest(
        @Size(max = 255) String title,
        @Size(max = 10000) String description,
        Long categoryId,
        Long teacherId,
        @Min(1) @Max(10000) Integer duration,
        LocalDate startDate,
        Set<@Size(max = 80) String> tags) {}

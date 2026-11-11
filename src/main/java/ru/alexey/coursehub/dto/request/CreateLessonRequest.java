package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLessonRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 50000) String content,
        @Size(max = 500) String videoUrl) {}

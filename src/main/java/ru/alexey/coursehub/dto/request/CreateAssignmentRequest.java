package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CreateAssignmentRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 20000) String description,
        LocalDateTime dueDate,
        @NotNull @Min(1) @Max(1000) Integer maxScore) {}

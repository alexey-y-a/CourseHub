package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record UpdateAssignmentRequest(
        @Size(max = 255) String title,
        @Size(max = 20000) String description,
        LocalDateTime dueDate,
        @Min(1) @Max(1000) Integer maxScore) {}

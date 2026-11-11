package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateModuleRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 10000) String description,
        @Min(1) Integer orderIndex) {}

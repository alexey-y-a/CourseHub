package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateModuleRequest(
        @Size(max = 255) String title,
        @Size(max = 10000) String description,
        @Min(1) Integer orderIndex) {}

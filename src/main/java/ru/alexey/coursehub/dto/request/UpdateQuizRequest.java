package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateQuizRequest(
        @Size(max = 255) String title, @Min(1) @Max(600) Integer timeLimit) {}

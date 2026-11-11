package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitAssignmentRequest(
        @NotBlank @Size(max = 50000) String content) {}

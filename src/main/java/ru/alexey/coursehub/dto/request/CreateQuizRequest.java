package ru.alexey.coursehub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateQuizRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull @Min(1) @Max(600) Integer timeLimit,
        @NotEmpty @Valid List<CreateQuestionRequest> questions) {}

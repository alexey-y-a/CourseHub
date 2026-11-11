package ru.alexey.coursehub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import ru.alexey.coursehub.entity.enums.QuestionType;

public record CreateQuestionRequest(
        @NotBlank @Size(max = 2000) String text,
        @NotNull QuestionType type,
        @NotEmpty @Valid List<CreateOptionRequest> options) {}

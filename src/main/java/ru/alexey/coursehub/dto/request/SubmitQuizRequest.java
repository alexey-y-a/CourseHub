package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public record SubmitQuizRequest(@NotEmpty Map<Long, List<Long>> answers) {}

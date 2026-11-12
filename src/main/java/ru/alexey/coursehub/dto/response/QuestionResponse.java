package ru.alexey.coursehub.dto.response;

import java.util.List;
import ru.alexey.coursehub.entity.enums.QuestionType;

public record QuestionResponse(Long id, String text, QuestionType type, List<OptionResponse> options) {}

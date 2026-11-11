package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOptionRequest(@NotBlank @Size(max = 500) String text, boolean correct) {}

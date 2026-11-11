package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.NotNull;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;

public record UpdateEnrollmentRequest(@NotNull EnrollmentStatus status) {}

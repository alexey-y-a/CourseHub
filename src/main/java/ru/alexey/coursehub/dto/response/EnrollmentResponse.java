package ru.alexey.coursehub.dto.response;

import java.time.LocalDate;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;

public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseTitle,
        Long studentId,
        String studentName,
        LocalDate enrollDate,
        EnrollmentStatus status) {}

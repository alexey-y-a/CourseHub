package ru.alexey.coursehub.dto.response;

import java.time.LocalDateTime;
import ru.alexey.coursehub.entity.enums.NotificationType;

public record NotificationResponse(
        Long id, NotificationType type, String message, boolean read, LocalDateTime createdAt) {}

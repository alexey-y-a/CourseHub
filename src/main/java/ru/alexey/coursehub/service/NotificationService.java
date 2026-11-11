package ru.alexey.coursehub.service;

import java.util.List;
import ru.alexey.coursehub.entity.Notification;
import ru.alexey.coursehub.entity.enums.NotificationType;

public interface NotificationService {

    Notification notify(Long userId, NotificationType type, String message);

    List<Notification> listByUser(Long userId);

    List<Notification> listUnread(Long userId);

    long countUnread(Long userId);

    int markAllRead(Long userId);
}

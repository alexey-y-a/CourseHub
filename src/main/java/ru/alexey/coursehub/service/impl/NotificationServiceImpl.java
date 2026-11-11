package ru.alexey.coursehub.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.entity.Notification;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.NotificationType;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.NotificationRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Notification notify(Long userId, NotificationType type, String message) {
        User user = userRepository.findById(userId).orElseThrow(() -> NotFoundException.of("User", userId));
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        log.debug("Notification {} created for user {}", saved.getId(), userId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listUnread(Long userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public int markAllRead(Long userId) {
        int updated = notificationRepository.markAllReadByUserId(userId);
        log.info("Marked {} notifications as read for user {}", updated, userId);
        return updated;
    }
}

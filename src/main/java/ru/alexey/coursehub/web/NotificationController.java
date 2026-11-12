package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.response.CountResponse;
import ru.alexey.coursehub.dto.response.NotificationResponse;
import ru.alexey.coursehub.mapper.UserMapper;
import ru.alexey.coursehub.service.NotificationService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/api/users/{userId}/notifications")
    @Operation(summary = "List notifications of user")
    public List<NotificationResponse> list(@PathVariable Long userId) {
        return notificationService.listByUser(userId).stream()
                .map(UserMapper::toNotification)
                .toList();
    }

    @GetMapping("/api/users/{userId}/notifications/unread")
    @Operation(summary = "List unread notifications of user")
    public List<NotificationResponse> unread(@PathVariable Long userId) {
        return notificationService.listUnread(userId).stream()
                .map(UserMapper::toNotification)
                .toList();
    }

    @GetMapping("/api/users/{userId}/notifications/unread-count")
    @Operation(summary = "Count unread notifications")
    public CountResponse unreadCount(@PathVariable Long userId) {
        return new CountResponse(notificationService.countUnread(userId));
    }

    @PostMapping("/api/users/{userId}/notifications/read")
    @Operation(summary = "Mark all notifications as read")
    public CountResponse markAllRead(@PathVariable Long userId) {
        return new CountResponse(notificationService.markAllRead(userId));
    }
}

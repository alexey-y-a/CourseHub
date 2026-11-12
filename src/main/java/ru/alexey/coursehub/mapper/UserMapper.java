package ru.alexey.coursehub.mapper;

import ru.alexey.coursehub.dto.response.NotificationResponse;
import ru.alexey.coursehub.dto.response.ProfileResponse;
import ru.alexey.coursehub.dto.response.UserResponse;
import ru.alexey.coursehub.entity.Notification;
import ru.alexey.coursehub.entity.Profile;
import ru.alexey.coursehub.entity.User;

public final class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(), user.getName(), user.getEmail(), user.getRole(), null, user.getCreatedAt());
    }

    public static UserResponse toResponseWithProfile(User user) {
        Profile profile = user.getProfile();
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                profile == null ? null : toProfile(profile),
                user.getCreatedAt());
    }

    public static ProfileResponse toProfile(Profile profile) {
        return new ProfileResponse(profile.getId(), profile.getBio(), profile.getAvatarUrl(), profile.getContacts());
    }

    public static NotificationResponse toNotification(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}

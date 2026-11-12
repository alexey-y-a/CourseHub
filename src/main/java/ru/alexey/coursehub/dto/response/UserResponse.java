package ru.alexey.coursehub.dto.response;

import java.time.LocalDateTime;
import ru.alexey.coursehub.entity.enums.Role;

public record UserResponse(
        Long id, String name, String email, Role role, ProfileResponse profile, LocalDateTime createdAt) {}

package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import ru.alexey.coursehub.entity.enums.Role;

public record UpdateUserRequest(
        @Size(max = 120) String name,
        @Email @Size(max = 180) String email,
        Role role) {}

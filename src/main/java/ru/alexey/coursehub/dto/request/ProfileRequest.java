package ru.alexey.coursehub.dto.request;

import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @Size(max = 5000) String bio,
        @Size(max = 500) String avatarUrl,
        @Size(max = 120) String contacts) {}

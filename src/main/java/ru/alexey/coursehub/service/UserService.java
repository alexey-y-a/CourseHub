package ru.alexey.coursehub.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.alexey.coursehub.dto.request.CreateUserRequest;
import ru.alexey.coursehub.dto.request.ProfileRequest;
import ru.alexey.coursehub.dto.request.UpdateUserRequest;
import ru.alexey.coursehub.entity.Profile;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;

public interface UserService {

    User create(CreateUserRequest request);

    User update(Long id, UpdateUserRequest request);

    User get(Long id);

    User getWithProfile(Long id);

    Page<User> list(Role role, String search, Pageable pageable);

    Profile saveProfile(Long userId, ProfileRequest request);

    void delete(Long id);
}

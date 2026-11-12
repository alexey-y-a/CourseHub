package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.CreateUserRequest;
import ru.alexey.coursehub.dto.request.ProfileRequest;
import ru.alexey.coursehub.dto.request.UpdateUserRequest;
import ru.alexey.coursehub.dto.response.PageResponse;
import ru.alexey.coursehub.dto.response.ProfileResponse;
import ru.alexey.coursehub.dto.response.UserResponse;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.mapper.UserMapper;
import ru.alexey.coursehub.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create user")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return UserMapper.toResponse(userService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user with profile")
    public UserResponse get(@PathVariable Long id) {
        return UserMapper.toResponseWithProfile(userService.getWithProfile(id));
    }

    @GetMapping
    @Operation(summary = "List users with optional role and name filters")
    public PageResponse<UserResponse> list(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(userService.list(role, search, pageable), UserMapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return UserMapper.toResponse(userService.update(id, request));
    }

    @PutMapping("/{id}/profile")
    @Operation(summary = "Create or update user profile")
    public ProfileResponse saveProfile(@PathVariable Long id, @Valid @RequestBody ProfileRequest request) {
        return UserMapper.toProfile(userService.saveProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}

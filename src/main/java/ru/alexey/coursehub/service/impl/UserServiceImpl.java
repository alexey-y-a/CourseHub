package ru.alexey.coursehub.service.impl;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.alexey.coursehub.dto.request.CreateUserRequest;
import ru.alexey.coursehub.dto.request.ProfileRequest;
import ru.alexey.coursehub.dto.request.UpdateUserRequest;
import ru.alexey.coursehub.entity.Profile;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.ProfileRepository;
import ru.alexey.coursehub.repository.QuizSubmissionRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;

    @Override
    @Transactional
    public User create(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email " + email + " already exists");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setRole(request.role());
        User saved = userRepository.save(user);
        log.info("Created user {} with role {}", saved.getId(), saved.getRole());
        return saved;
    }

    @Override
    @Transactional
    public User update(Long id, UpdateUserRequest request) {
        User user = get(id);
        if (StringUtils.hasText(request.name())) {
            user.setName(request.name().trim());
        }
        if (StringUtils.hasText(request.email())) {
            String email = normalizeEmail(request.email());
            if (!email.equals(user.getEmail())) {
                userRepository.findByEmail(email).ifPresent(existing -> {
                    throw new ConflictException("User with email " + email + " already exists");
                });
                user.setEmail(email);
            }
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        log.info("Updated user {}", id);
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User get(Long id) {
        return userRepository.findById(id).orElseThrow(() -> NotFoundException.of("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public User getWithProfile(Long id) {
        return userRepository.findByIdWithProfile(id).orElseThrow(() -> NotFoundException.of("User", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> list(Role role, String search, Pageable pageable) {
        String text = StringUtils.hasText(search) ? search.trim() : null;
        if (role != null && text != null) {
            return userRepository.findByRoleAndNameContainingIgnoreCase(role, text, pageable);
        }
        if (role != null) {
            return userRepository.findByRole(role, pageable);
        }
        if (text != null) {
            return userRepository.searchByName(text, pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Profile saveProfile(Long userId, ProfileRequest request) {
        User user = get(userId);
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
        }
        profile.setBio(request.bio());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setContacts(request.contacts());
        Profile saved = profileRepository.save(profile);
        log.info("Saved profile for user {}", userId);
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = get(id);
        if (courseRepository.existsByTeacherId(id)) {
            throw new ConflictException("User " + id + " teaches courses and cannot be deleted");
        }
        if (enrollmentRepository.existsByStudentId(id)) {
            throw new ConflictException("User " + id + " has enrollments and cannot be deleted");
        }
        if (submissionRepository.existsByStudentId(id)) {
            throw new ConflictException("User " + id + " has submissions and cannot be deleted");
        }
        if (quizSubmissionRepository.existsByStudentId(id)) {
            throw new ConflictException("User " + id + " has quiz results and cannot be deleted");
        }
        userRepository.delete(user);
        log.info("Deleted user {}", id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.dto.request.CreateUserRequest;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.ProfileRepository;
import ru.alexey.coursehub.repository.QuizSubmissionRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private QuizSubmissionRepository quizSubmissionRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createsUserWithNormalizedEmail() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User created = userService.create(new CreateUserRequest("  Anna  ", "  Student@Example.COM  ", Role.STUDENT));

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getName()).isEqualTo("Anna");
        assertThat(created.getEmail()).isEqualTo("student@example.com");
        assertThat(created.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("Anna", "student@example.com", Role.STUDENT)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void blocksDeleteWhenUserTeachesCourses() {
        User teacher = new User();
        teacher.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(teacher));
        when(courseRepository.existsByTeacherId(5L)).thenReturn(true);

        assertThatThrownBy(() -> userService.delete(5L)).isInstanceOf(ConflictException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deletesUserWithoutDependencies() {
        User student = new User();
        student.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(student));
        when(courseRepository.existsByTeacherId(7L)).thenReturn(false);
        when(enrollmentRepository.existsByStudentId(7L)).thenReturn(false);
        when(submissionRepository.existsByStudentId(7L)).thenReturn(false);
        when(quizSubmissionRepository.existsByStudentId(7L)).thenReturn(false);

        userService.delete(7L);

        verify(userRepository).delete(student);
    }
}

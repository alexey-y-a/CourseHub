package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;
import ru.alexey.coursehub.entity.enums.NotificationType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.EnrollmentServiceImpl;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void enrollsStudentAndSendsNotification() {
        Course course = new Course();
        course.setId(1L);
        course.setTitle("Hibernate");
        User student = new User();
        student.setId(2L);
        student.setName("Anna");
        student.setRole(Role.STUDENT);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setId(50L);
            return enrollment;
        });

        Enrollment enrollment = enrollmentService.enroll(1L, 2L);

        assertThat(enrollment.getId()).isEqualTo(50L);
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getStudent()).isSameAs(student);
        assertThat(enrollment.getEnrollDate()).isNotNull();
        verify(notificationService).notify(eq(2L), eq(NotificationType.ENROLLMENT), any());
    }

    @Test
    void rejectsDuplicateEnrollment() {
        Course course = new Course();
        course.setId(1L);
        User student = new User();
        student.setId(2L);
        student.setRole(Role.STUDENT);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByCourseIdAndStudentId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 2L)).isInstanceOf(ConflictException.class);

        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void rejectsEnrollmentOfTeacher() {
        Course course = new Course();
        course.setId(1L);
        User teacher = new User();
        teacher.setId(3L);
        teacher.setRole(Role.TEACHER);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> enrollmentService.enroll(1L, 3L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a student");
    }
}

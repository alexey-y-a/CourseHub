package ru.alexey.coursehub.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.EnrollmentService;
import ru.alexey.coursehub.support.TestData;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnrollmentServiceIT {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    private Course course;
    private User student;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "teacher@example.com", Role.TEACHER));
        student = userRepository.save(TestData.user("Anna", "student@example.com", Role.STUDENT));
        course = courseRepository.save(TestData.course("Hibernate", category, teacher));
    }

    @Test
    void enrollsStudentAndListsBothSides() {
        Enrollment enrollment = enrollmentService.enroll(course.getId(), student.getId());

        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getEnrollDate()).isNotNull();
        assertThat(enrollmentService.listByCourse(course.getId())).hasSize(1);
        assertThat(enrollmentService.listByStudent(student.getId())).hasSize(1);
        assertThat(enrollmentService
                        .pageByStudent(student.getId(), null, PageRequest.of(0, 10))
                        .getTotalElements())
                .isEqualTo(1);
        assertThat(enrollmentService.countByCourse(course.getId())).isEqualTo(1);
    }

    @Test
    void blocksSecondEnrollmentOnSameCourse() {
        enrollmentService.enroll(course.getId(), student.getId());

        assertThatThrownBy(() -> enrollmentService.enroll(course.getId(), student.getId()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void cancelsEnrollment() {
        enrollmentService.enroll(course.getId(), student.getId());

        enrollmentService.cancel(course.getId(), student.getId());

        assertThat(enrollmentService.listByCourse(course.getId())).isEmpty();
        assertThat(enrollmentService.countByCourse(course.getId())).isZero();
    }
}

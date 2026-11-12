package ru.alexey.coursehub.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.CourseService;
import ru.alexey.coursehub.support.TestData;

@SpringBootTest
@ActiveProfiles("test")
class LazyLoadingIT {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private Long courseId;
    private Long teacherId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.executeWithoutResult(status -> {
            Category category = categoryRepository.save(TestData.category("Programming"));
            User teacher = userRepository.save(
                    TestData.user("Ivan", "lazy-" + UUID.randomUUID() + "@example.com", Role.TEACHER));
            Course course = TestData.course("Hibernate", category, teacher);
            Module module = TestData.module(course, "Intro", 1);
            course.getModules().add(module);
            Lesson lesson = TestData.lesson(module, "What is ORM");
            module.getLessons().add(lesson);
            courseRepository.saveAndFlush(course);
            courseId = course.getId();
            teacherId = teacher.getId();
            categoryId = category.getId();
        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.executeWithoutResult(status -> {
            courseRepository.deleteById(courseId);
            userRepository.deleteById(teacherId);
            categoryRepository.deleteById(categoryId);
        });
    }

    @Test
    void accessingLazyCollectionOutsideTransactionFails() {
        Course course = courseRepository.findById(courseId).orElseThrow();

        assertThatThrownBy(() -> course.getModules().size()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void fetchJoinLoadsCollectionBeforeSessionCloses() {
        Course course = transactionTemplate.execute(
                status -> courseRepository.findByIdWithModules(courseId).orElseThrow());

        assertThat(course.getModules()).hasSize(1);
    }

    @Test
    void serviceInitializesNestedCollectionsInsideTransaction() {
        Course structure = courseService.getWithModules(courseId);

        assertThat(structure.getModules()).hasSize(1);
        assertThat(structure.getModules().get(0).getLessons()).hasSize(1);
        assertThat(structure.getModules().get(0).getLessons().get(0).getTitle()).isEqualTo("What is ORM");
    }
}

package ru.alexey.coursehub.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Submission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.repository.AssignmentRepository;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.LessonRepository;
import ru.alexey.coursehub.repository.ModuleRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.support.TestData;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryCrudIT {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void persistsCourseGraphThroughCascade() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "teacher@example.com", Role.TEACHER));
        Course course = TestData.course("Hibernate", category, teacher);
        Module module = TestData.module(course, "Intro", 1);
        course.getModules().add(module);
        Lesson lesson = TestData.lesson(module, "What is ORM");
        module.getLessons().add(lesson);
        Assignment assignment = TestData.assignment(lesson, "Homework", 100);
        lesson.getAssignments().add(assignment);

        Course saved = courseRepository.save(course);
        entityManager.flush();
        entityManager.clear();

        Course reloaded = courseRepository.findByIdWithModules(saved.getId()).orElseThrow();
        assertThat(reloaded.getModules()).hasSize(1);

        Module reloadedModule =
                moduleRepository.findByIdWithLessons(module.getId()).orElseThrow();
        assertThat(reloadedModule.getLessons()).hasSize(1);

        Lesson reloadedLesson =
                lessonRepository.findByIdWithAssignments(lesson.getId()).orElseThrow();
        assertThat(reloadedLesson.getAssignments()).hasSize(1);
        assertThat(reloadedLesson.getAssignments().get(0).getMaxScore()).isEqualTo(100);
    }

    @Test
    void updatesCourseThroughRepository() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "update@example.com", Role.TEACHER));
        Course course = courseRepository.save(TestData.course("Old title", category, teacher));
        entityManager.flush();
        entityManager.clear();

        Course loaded = courseRepository.findByIdWithDetails(course.getId()).orElseThrow();
        loaded.setTitle("New title");
        loaded.setDuration(20);
        entityManager.flush();
        entityManager.clear();

        Course updated = courseRepository.findByIdWithDetails(course.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("New title");
        assertThat(updated.getDuration()).isEqualTo(20);
    }

    @Test
    void deletesCourseWithModulesAndLessons() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "delete@example.com", Role.TEACHER));
        Course course = TestData.course("Hibernate", category, teacher);
        Module module = TestData.module(course, "Intro", 1);
        course.getModules().add(module);
        Lesson lesson = TestData.lesson(module, "What is ORM");
        module.getLessons().add(lesson);
        courseRepository.saveAndFlush(course);
        Long courseId = course.getId();
        Long moduleId = module.getId();

        Course managed = courseRepository.findById(courseId).orElseThrow();
        courseRepository.delete(managed);
        entityManager.flush();

        assertThat(courseRepository.findById(courseId)).isEmpty();
        assertThat(moduleRepository.countByCourseId(courseId)).isZero();
        assertThat(lessonRepository.countByModuleId(moduleId)).isZero();
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(TestData.user("Anna", "dup@example.com", Role.STUDENT));

        assertThatThrownBy(() -> userRepository.saveAndFlush(TestData.user("Copy", "dup@example.com", Role.TEACHER)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateEnrollment() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "enroll@example.com", Role.TEACHER));
        User student = userRepository.save(TestData.user("Anna", "enroll-student@example.com", Role.STUDENT));
        Course course = courseRepository.save(TestData.course("Hibernate", category, teacher));

        Enrollment first = new Enrollment();
        first.setCourse(course);
        first.setStudent(student);
        first.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRepository.saveAndFlush(first);

        Enrollment second = new Enrollment();
        second.setCourse(course);
        second.setStudent(student);
        second.setStatus(EnrollmentStatus.ACTIVE);

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateSubmission() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "sub@example.com", Role.TEACHER));
        User student = userRepository.save(TestData.user("Anna", "sub-student@example.com", Role.STUDENT));
        Course course = courseRepository.save(TestData.course("Hibernate", category, teacher));
        Module module = moduleRepository.save(TestData.module(course, "Intro", 1));
        Lesson lesson = lessonRepository.save(TestData.lesson(module, "ORM"));
        Assignment assignment = assignmentRepository.save(TestData.assignment(lesson, "Homework", 100));

        Submission first = new Submission();
        first.setAssignment(assignment);
        first.setStudent(student);
        first.setContent("first");
        submissionRepository.saveAndFlush(first);

        Submission second = new Submission();
        second.setAssignment(assignment);
        second.setStudent(student);
        second.setContent("second");

        assertThatThrownBy(() -> submissionRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

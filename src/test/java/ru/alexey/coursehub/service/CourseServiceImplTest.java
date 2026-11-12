package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.dto.request.CreateCourseRequest;
import ru.alexey.coursehub.dto.request.CreateModuleRequest;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Tag;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.EnrollmentRepository;
import ru.alexey.coursehub.repository.LessonRepository;
import ru.alexey.coursehub.repository.ModuleRepository;
import ru.alexey.coursehub.repository.TagRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.CourseServiceImpl;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseServiceImpl courseService;

    @Test
    void createsCourseWithExistingAndNewTags() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Programming");
        User teacher = new User();
        teacher.setId(2L);
        teacher.setRole(Role.TEACHER);
        Tag existing = new Tag();
        existing.setId(10L);
        existing.setName("Java");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(tagRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(existing));
        when(tagRepository.findByNameIgnoreCase("Hibernate")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            tag.setId(11L);
            return tag;
        });
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> {
            Course course = invocation.getArgument(0);
            course.setId(100L);
            return course;
        });

        Course created = courseService.create(
                new CreateCourseRequest("  Hibernate  ", "desc", 1L, 2L, 10, null, Set.of("Java", "Hibernate")));

        assertThat(created.getId()).isEqualTo(100L);
        assertThat(created.getTitle()).isEqualTo("Hibernate");
        assertThat(created.getCategory()).isSameAs(category);
        assertThat(created.getTeacher()).isSameAs(teacher);
        assertThat(created.getTags()).extracting(Tag::getName).containsExactlyInAnyOrder("Java", "Hibernate");
    }

    @Test
    void rejectsMissingCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.create(new CreateCourseRequest("Course", null, 99L, 2L, 5, null, null)))
                .isInstanceOf(NotFoundException.class);

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void rejectsTeacherWithStudentRole() {
        Category category = new Category();
        category.setId(1L);
        User student = new User();
        student.setId(3L);
        student.setRole(Role.STUDENT);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findById(3L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> courseService.create(new CreateCourseRequest("Course", null, 1L, 3L, 5, null, null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("not a teacher");
    }

    @Test
    void rejectsDuplicateModuleOrderIndex() {
        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(course));
        when(moduleRepository.existsByCourseIdAndOrderIndex(1L, 2)).thenReturn(true);

        assertThatThrownBy(() -> courseService.addModule(1L, new CreateModuleRequest("Module", null, 2)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void blocksCourseDeletionWhenStudentsEnrolled() {
        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.existsByCourseId(1L)).thenReturn(true);

        assertThatThrownBy(() -> courseService.delete(1L)).isInstanceOf(ConflictException.class);

        verify(courseRepository, never()).delete(any(Course.class));
    }
}

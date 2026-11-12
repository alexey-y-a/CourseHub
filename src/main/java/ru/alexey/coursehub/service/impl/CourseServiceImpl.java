package ru.alexey.coursehub.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.alexey.coursehub.dto.request.CreateCourseRequest;
import ru.alexey.coursehub.dto.request.CreateLessonRequest;
import ru.alexey.coursehub.dto.request.CreateModuleRequest;
import ru.alexey.coursehub.dto.request.UpdateCourseRequest;
import ru.alexey.coursehub.dto.request.UpdateLessonRequest;
import ru.alexey.coursehub.dto.request.UpdateModuleRequest;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
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
import ru.alexey.coursehub.service.CourseService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public Course create(CreateCourseRequest request) {
        Category category = findCategory(request.categoryId());
        User teacher = requireTeacher(request.teacherId());
        Course course = new Course();
        course.setTitle(request.title().trim());
        course.setDescription(request.description());
        course.setCategory(category);
        course.setTeacher(teacher);
        course.setDuration(request.duration());
        course.setStartDate(request.startDate());
        course.setTags(resolveTags(request.tags()));
        Course saved = courseRepository.save(course);
        log.info("Created course {} for teacher {}", saved.getId(), teacher.getId());
        return saved;
    }

    @Override
    @Transactional
    public Course update(Long id, UpdateCourseRequest request) {
        Course course = get(id);
        if (StringUtils.hasText(request.title())) {
            course.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.categoryId() != null) {
            course.setCategory(findCategory(request.categoryId()));
        }
        if (request.teacherId() != null) {
            course.setTeacher(requireTeacher(request.teacherId()));
        }
        if (request.duration() != null) {
            course.setDuration(request.duration());
        }
        if (request.startDate() != null) {
            course.setStartDate(request.startDate());
        }
        if (request.tags() != null) {
            course.getTags().clear();
            course.getTags().addAll(resolveTags(request.tags()));
        }
        log.info("Updated course {}", id);
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public Course get(Long id) {
        Course course = courseRepository.findByIdWithDetails(id).orElseThrow(() -> NotFoundException.of("Course", id));
        course.getTags().size();
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public Course getWithModules(Long id) {
        Course course = courseRepository.findByIdWithModules(id).orElseThrow(() -> NotFoundException.of("Course", id));
        course.getModules().forEach(module -> module.getLessons().size());
        return course;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Course> list(Long categoryId, Long teacherId, String title, Pageable pageable) {
        String text = StringUtils.hasText(title) ? title.trim() : null;
        return courseRepository.search(categoryId, teacherId, text, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Module> listModules(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw NotFoundException.of("Course", courseId);
        }
        return moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lesson> listLessons(Long moduleId) {
        if (!moduleRepository.existsById(moduleId)) {
            throw NotFoundException.of("Module", moduleId);
        }
        return lessonRepository.findByModuleIdOrderByIdAsc(moduleId);
    }

    @Override
    @Transactional
    public Module addModule(Long courseId, CreateModuleRequest request) {
        Course course = get(courseId);
        int orderIndex;
        if (request.orderIndex() != null) {
            if (moduleRepository.existsByCourseIdAndOrderIndex(courseId, request.orderIndex())) {
                throw new ConflictException("Module with order index " + request.orderIndex() + " already exists");
            }
            orderIndex = request.orderIndex();
        } else {
            orderIndex = moduleRepository.findMaxOrderIndex(courseId) + 1;
        }
        Module module = new Module();
        module.setCourse(course);
        module.setTitle(request.title().trim());
        module.setDescription(request.description());
        module.setOrderIndex(orderIndex);
        Module saved = moduleRepository.save(module);
        log.info("Added module {} to course {}", saved.getId(), courseId);
        return saved;
    }

    @Override
    @Transactional
    public Module updateModule(Long moduleId, UpdateModuleRequest request) {
        Module module = getModule(moduleId);
        if (StringUtils.hasText(request.title())) {
            module.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            module.setDescription(request.description());
        }
        if (request.orderIndex() != null && !request.orderIndex().equals(module.getOrderIndex())) {
            Long courseId = module.getCourse().getId();
            if (moduleRepository.existsByCourseIdAndOrderIndex(courseId, request.orderIndex())) {
                throw new ConflictException("Module with order index " + request.orderIndex() + " already exists");
            }
            module.setOrderIndex(request.orderIndex());
        }
        log.info("Updated module {}", moduleId);
        return module;
    }

    @Override
    @Transactional
    public void deleteModule(Long moduleId) {
        Module module = getModule(moduleId);
        moduleRepository.delete(module);
        log.info("Deleted module {}", moduleId);
    }

    @Override
    @Transactional
    public Lesson addLesson(Long moduleId, CreateLessonRequest request) {
        Module module = getModule(moduleId);
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.title().trim());
        lesson.setContent(request.content());
        lesson.setVideoUrl(request.videoUrl());
        Lesson saved = lessonRepository.save(lesson);
        log.info("Added lesson {} to module {}", saved.getId(), moduleId);
        return saved;
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long lessonId, UpdateLessonRequest request) {
        Lesson lesson = getLesson(lessonId);
        if (StringUtils.hasText(request.title())) {
            lesson.setTitle(request.title().trim());
        }
        if (request.content() != null) {
            lesson.setContent(request.content());
        }
        if (request.videoUrl() != null) {
            lesson.setVideoUrl(request.videoUrl());
        }
        log.info("Updated lesson {}", lessonId);
        return lesson;
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = getLesson(lessonId);
        lessonRepository.delete(lesson);
        log.info("Deleted lesson {}", lessonId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Course course = get(id);
        if (enrollmentRepository.existsByCourseId(id)) {
            throw new ConflictException("Course " + id + " has enrolled students and cannot be deleted");
        }
        courseRepository.delete(course);
        log.info("Deleted course {}", id);
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> NotFoundException.of("Category", categoryId));
    }

    private User requireTeacher(Long teacherId) {
        User user = userRepository.findById(teacherId).orElseThrow(() -> NotFoundException.of("User", teacherId));
        if (user.getRole() != Role.TEACHER) {
            throw new BadRequestException("User " + teacherId + " is not a teacher");
        }
        return user;
    }

    private Module getModule(Long moduleId) {
        return moduleRepository.findById(moduleId).orElseThrow(() -> NotFoundException.of("Module", moduleId));
    }

    private Lesson getLesson(Long lessonId) {
        return lessonRepository.findById(lessonId).orElseThrow(() -> NotFoundException.of("Lesson", lessonId));
    }

    private Set<Tag> resolveTags(Set<String> names) {
        Set<Tag> tags = new HashSet<>();
        if (names == null) {
            return tags;
        }
        for (String raw : names) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String name = raw.trim();
            Tag tag = tagRepository.findByNameIgnoreCase(name).orElseGet(() -> {
                Tag created = new Tag();
                created.setName(name);
                return tagRepository.save(created);
            });
            tags.add(tag);
        }
        return tags;
    }
}

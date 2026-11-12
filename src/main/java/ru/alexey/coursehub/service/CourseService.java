package ru.alexey.coursehub.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.alexey.coursehub.dto.request.CreateCourseRequest;
import ru.alexey.coursehub.dto.request.CreateLessonRequest;
import ru.alexey.coursehub.dto.request.CreateModuleRequest;
import ru.alexey.coursehub.dto.request.UpdateCourseRequest;
import ru.alexey.coursehub.dto.request.UpdateLessonRequest;
import ru.alexey.coursehub.dto.request.UpdateModuleRequest;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;

public interface CourseService {

    Course create(CreateCourseRequest request);

    Course update(Long id, UpdateCourseRequest request);

    Course get(Long id);

    Course getWithModules(Long id);

    Page<Course> list(Long categoryId, Long teacherId, String title, Pageable pageable);

    Module addModule(Long courseId, CreateModuleRequest request);

    List<Module> listModules(Long courseId);

    List<Lesson> listLessons(Long moduleId);

    Module updateModule(Long moduleId, UpdateModuleRequest request);

    void deleteModule(Long moduleId);

    Lesson addLesson(Long moduleId, CreateLessonRequest request);

    Lesson updateLesson(Long lessonId, UpdateLessonRequest request);

    void deleteLesson(Long lessonId);

    void delete(Long id);
}

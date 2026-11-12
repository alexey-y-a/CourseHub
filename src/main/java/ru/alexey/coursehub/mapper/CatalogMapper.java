package ru.alexey.coursehub.mapper;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import ru.alexey.coursehub.dto.response.CategoryResponse;
import ru.alexey.coursehub.dto.response.CourseResponse;
import ru.alexey.coursehub.dto.response.CourseStructureResponse;
import ru.alexey.coursehub.dto.response.CourseSummaryResponse;
import ru.alexey.coursehub.dto.response.LessonResponse;
import ru.alexey.coursehub.dto.response.LessonStructureResponse;
import ru.alexey.coursehub.dto.response.ModuleResponse;
import ru.alexey.coursehub.dto.response.ModuleStructureResponse;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Tag;

public final class CatalogMapper {

    private CatalogMapper() {}

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }

    public static CourseSummaryResponse toSummary(Course course) {
        return new CourseSummaryResponse(
                course.getId(),
                course.getTitle(),
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getTeacher().getId(),
                course.getTeacher().getName(),
                course.getDuration(),
                course.getStartDate());
    }

    public static CourseResponse toResponse(Course course) {
        Set<String> tags = new LinkedHashSet<>();
        course.getTags().stream()
                .map(Tag::getName)
                .sorted(Comparator.naturalOrder())
                .forEach(tags::add);
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getCategory().getId(),
                course.getCategory().getName(),
                course.getTeacher().getId(),
                course.getTeacher().getName(),
                course.getDuration(),
                course.getStartDate(),
                tags,
                course.getCreatedAt());
    }

    public static CourseStructureResponse toStructure(Course course) {
        return new CourseStructureResponse(
                course.getId(),
                course.getTitle(),
                course.getModules().stream()
                        .sorted(Comparator.comparing(Module::getOrderIndex))
                        .map(CatalogMapper::toStructure)
                        .toList());
    }

    public static ModuleStructureResponse toStructure(Module module) {
        return new ModuleStructureResponse(
                module.getId(),
                module.getTitle(),
                module.getOrderIndex(),
                module.getDescription(),
                module.getLessons().stream()
                        .map(lesson ->
                                new LessonStructureResponse(lesson.getId(), lesson.getTitle(), lesson.getVideoUrl()))
                        .toList());
    }

    public static ModuleResponse toResponse(Module module) {
        return new ModuleResponse(
                module.getId(),
                module.getCourse().getId(),
                module.getTitle(),
                module.getOrderIndex(),
                module.getDescription());
    }

    public static LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getTitle(),
                lesson.getContent(),
                lesson.getVideoUrl());
    }
}

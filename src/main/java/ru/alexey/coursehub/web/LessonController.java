package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.CreateLessonRequest;
import ru.alexey.coursehub.dto.request.UpdateLessonRequest;
import ru.alexey.coursehub.dto.response.LessonResponse;
import ru.alexey.coursehub.mapper.CatalogMapper;
import ru.alexey.coursehub.service.CourseService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lessons")
public class LessonController {

    private final CourseService courseService;

    @PostMapping("/api/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add lesson to module")
    public LessonResponse create(@PathVariable Long moduleId, @Valid @RequestBody CreateLessonRequest request) {
        return CatalogMapper.toResponse(courseService.addLesson(moduleId, request));
    }

    @GetMapping("/api/modules/{moduleId}/lessons")
    @Operation(summary = "List lessons of module")
    public List<LessonResponse> list(@PathVariable Long moduleId) {
        return courseService.listLessons(moduleId).stream()
                .map(CatalogMapper::toResponse)
                .toList();
    }

    @PutMapping("/api/lessons/{lessonId}")
    @Operation(summary = "Update lesson")
    public LessonResponse update(@PathVariable Long lessonId, @Valid @RequestBody UpdateLessonRequest request) {
        return CatalogMapper.toResponse(courseService.updateLesson(lessonId, request));
    }

    @DeleteMapping("/api/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete lesson with assignments")
    public void delete(@PathVariable Long lessonId) {
        courseService.deleteLesson(lessonId);
    }
}

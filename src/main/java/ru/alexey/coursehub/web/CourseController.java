package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.CreateCourseRequest;
import ru.alexey.coursehub.dto.request.UpdateCourseRequest;
import ru.alexey.coursehub.dto.response.CourseResponse;
import ru.alexey.coursehub.dto.response.CourseStructureResponse;
import ru.alexey.coursehub.dto.response.CourseSummaryResponse;
import ru.alexey.coursehub.dto.response.PageResponse;
import ru.alexey.coursehub.mapper.CatalogMapper;
import ru.alexey.coursehub.service.CourseService;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create course with category, teacher and tags")
    public CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        return CatalogMapper.toResponse(courseService.create(request));
    }

    @GetMapping
    @Operation(summary = "List courses with filters")
    public PageResponse<CourseSummaryResponse> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String title,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(courseService.list(categoryId, teacherId, title, pageable), CatalogMapper::toSummary);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course")
    public CourseResponse get(@PathVariable Long id) {
        return CatalogMapper.toResponse(courseService.get(id));
    }

    @GetMapping("/{id}/structure")
    @Operation(summary = "Get course with modules and lessons")
    public CourseStructureResponse structure(@PathVariable Long id) {
        return CatalogMapper.toStructure(courseService.getWithModules(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update course")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCourseRequest request) {
        return CatalogMapper.toResponse(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete course without enrolled students")
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }
}

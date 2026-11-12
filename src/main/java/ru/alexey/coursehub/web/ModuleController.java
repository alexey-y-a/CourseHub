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
import ru.alexey.coursehub.dto.request.CreateModuleRequest;
import ru.alexey.coursehub.dto.request.UpdateModuleRequest;
import ru.alexey.coursehub.dto.response.ModuleResponse;
import ru.alexey.coursehub.mapper.CatalogMapper;
import ru.alexey.coursehub.service.CourseService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Modules")
public class ModuleController {

    private final CourseService courseService;

    @PostMapping("/api/courses/{courseId}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add module to course")
    public ModuleResponse create(@PathVariable Long courseId, @Valid @RequestBody CreateModuleRequest request) {
        return CatalogMapper.toResponse(courseService.addModule(courseId, request));
    }

    @GetMapping("/api/courses/{courseId}/modules")
    @Operation(summary = "List modules of course")
    public List<ModuleResponse> list(@PathVariable Long courseId) {
        return courseService.listModules(courseId).stream()
                .map(CatalogMapper::toResponse)
                .toList();
    }

    @PutMapping("/api/modules/{moduleId}")
    @Operation(summary = "Update module")
    public ModuleResponse update(@PathVariable Long moduleId, @Valid @RequestBody UpdateModuleRequest request) {
        return CatalogMapper.toResponse(courseService.updateModule(moduleId, request));
    }

    @DeleteMapping("/api/modules/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete module with lessons")
    public void delete(@PathVariable Long moduleId) {
        courseService.deleteModule(moduleId);
    }
}

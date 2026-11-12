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
import ru.alexey.coursehub.dto.request.CreateAssignmentRequest;
import ru.alexey.coursehub.dto.request.UpdateAssignmentRequest;
import ru.alexey.coursehub.dto.response.AssignmentResponse;
import ru.alexey.coursehub.mapper.LearningMapper;
import ru.alexey.coursehub.service.AssignmentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/api/lessons/{lessonId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create assignment for lesson")
    public AssignmentResponse create(@PathVariable Long lessonId, @Valid @RequestBody CreateAssignmentRequest request) {
        return LearningMapper.toResponse(assignmentService.create(lessonId, request));
    }

    @GetMapping("/api/lessons/{lessonId}/assignments")
    @Operation(summary = "List assignments of lesson")
    public List<AssignmentResponse> list(@PathVariable Long lessonId) {
        return assignmentService.listByLesson(lessonId).stream()
                .map(LearningMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/assignments/{id}")
    @Operation(summary = "Get assignment")
    public AssignmentResponse get(@PathVariable Long id) {
        return LearningMapper.toResponse(assignmentService.get(id));
    }

    @PutMapping("/api/assignments/{id}")
    @Operation(summary = "Update assignment")
    public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody UpdateAssignmentRequest request) {
        return LearningMapper.toResponse(assignmentService.update(id, request));
    }

    @DeleteMapping("/api/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete assignment without submissions")
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }
}

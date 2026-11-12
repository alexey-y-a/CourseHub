package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.GradeSubmissionRequest;
import ru.alexey.coursehub.dto.request.SubmitAssignmentRequest;
import ru.alexey.coursehub.dto.response.PageResponse;
import ru.alexey.coursehub.dto.response.SubmissionResponse;
import ru.alexey.coursehub.mapper.LearningMapper;
import ru.alexey.coursehub.service.SubmissionService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/api/assignments/{assignmentId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit assignment solution")
    public SubmissionResponse submit(
            @PathVariable Long assignmentId,
            @RequestParam Long studentId,
            @Valid @RequestBody SubmitAssignmentRequest request) {
        return LearningMapper.toResponse(submissionService.submit(assignmentId, studentId, request));
    }

    @GetMapping("/api/assignments/{assignmentId}/submissions")
    @Operation(summary = "List submissions of assignment")
    public List<SubmissionResponse> listByAssignment(@PathVariable Long assignmentId) {
        return submissionService.listByAssignment(assignmentId).stream()
                .map(LearningMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/submissions/{id}")
    @Operation(summary = "Get submission")
    public SubmissionResponse get(@PathVariable Long id) {
        return LearningMapper.toResponse(submissionService.get(id));
    }

    @PatchMapping("/api/submissions/{id}/grade")
    @Operation(summary = "Grade submission")
    public SubmissionResponse grade(@PathVariable Long id, @Valid @RequestBody GradeSubmissionRequest request) {
        return LearningMapper.toResponse(submissionService.grade(id, request));
    }

    @GetMapping("/api/users/{studentId}/submissions")
    @Operation(summary = "List submissions of student")
    public PageResponse<SubmissionResponse> listByStudent(
            @PathVariable Long studentId, @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(submissionService.pageByStudent(studentId, pageable), LearningMapper::toResponse);
    }

    @GetMapping("/api/users/{studentId}/courses/{courseId}/submissions")
    @Operation(summary = "List submissions of student in course")
    public List<SubmissionResponse> listByStudentAndCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        return submissionService.listByStudentAndCourse(studentId, courseId).stream()
                .map(LearningMapper::toResponse)
                .toList();
    }
}

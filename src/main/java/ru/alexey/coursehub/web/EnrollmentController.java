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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.UpdateEnrollmentRequest;
import ru.alexey.coursehub.dto.response.EnrollmentResponse;
import ru.alexey.coursehub.dto.response.PageResponse;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;
import ru.alexey.coursehub.mapper.LearningMapper;
import ru.alexey.coursehub.service.EnrollmentService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/api/courses/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enroll student in course")
    public EnrollmentResponse enroll(@PathVariable Long courseId, @RequestParam Long studentId) {
        return LearningMapper.toResponse(enrollmentService.enroll(courseId, studentId));
    }

    @GetMapping("/api/courses/{courseId}/enrollments")
    @Operation(summary = "List students enrolled in course")
    public List<EnrollmentResponse> listByCourse(@PathVariable Long courseId) {
        return enrollmentService.listByCourse(courseId).stream()
                .map(LearningMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/api/courses/{courseId}/enrollments/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cancel enrollment of student in course")
    public void cancel(@PathVariable Long courseId, @PathVariable Long studentId) {
        enrollmentService.cancel(courseId, studentId);
    }

    @GetMapping("/api/enrollments/{id}")
    @Operation(summary = "Get enrollment")
    public EnrollmentResponse get(@PathVariable Long id) {
        return LearningMapper.toResponse(enrollmentService.get(id));
    }

    @PatchMapping("/api/enrollments/{id}")
    @Operation(summary = "Change enrollment status")
    public EnrollmentResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateEnrollmentRequest request) {
        return LearningMapper.toResponse(enrollmentService.updateStatus(id, request));
    }

    @DeleteMapping("/api/enrollments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete enrollment")
    public void delete(@PathVariable Long id) {
        enrollmentService.cancel(id);
    }

    @GetMapping("/api/users/{studentId}/enrollments")
    @Operation(summary = "List courses of student")
    public PageResponse<EnrollmentResponse> listByStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) EnrollmentStatus status,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(
                enrollmentService.pageByStudent(studentId, status, pageable), LearningMapper::toResponse);
    }
}

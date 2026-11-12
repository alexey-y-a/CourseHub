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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.ReviewRequest;
import ru.alexey.coursehub.dto.response.AverageResponse;
import ru.alexey.coursehub.dto.response.PageResponse;
import ru.alexey.coursehub.dto.response.ReviewResponse;
import ru.alexey.coursehub.mapper.LearningMapper;
import ru.alexey.coursehub.service.ReviewService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/courses/{courseId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add course review")
    public ReviewResponse add(
            @PathVariable Long courseId, @RequestParam Long studentId, @Valid @RequestBody ReviewRequest request) {
        return LearningMapper.toResponse(reviewService.add(courseId, studentId, request));
    }

    @GetMapping("/api/courses/{courseId}/reviews")
    @Operation(summary = "List course reviews")
    public PageResponse<ReviewResponse> list(
            @PathVariable Long courseId, @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return PageResponse.from(reviewService.listByCourse(courseId, pageable), LearningMapper::toResponse);
    }

    @GetMapping("/api/courses/{courseId}/rating")
    @Operation(summary = "Average course rating")
    public AverageResponse rating(@PathVariable Long courseId) {
        return new AverageResponse(reviewService.averageRating(courseId));
    }

    @DeleteMapping("/api/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete review")
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }
}

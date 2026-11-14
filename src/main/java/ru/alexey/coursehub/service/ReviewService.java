package ru.alexey.coursehub.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.alexey.coursehub.dto.request.ReviewRequest;
import ru.alexey.coursehub.dto.request.UpdateReviewRequest;
import ru.alexey.coursehub.entity.CourseReview;

public interface ReviewService {

    CourseReview add(Long courseId, Long studentId, ReviewRequest request);

    CourseReview update(Long id, UpdateReviewRequest request);

    Page<CourseReview> listByCourse(Long courseId, Pageable pageable);

    Double averageRating(Long courseId);

    void delete(Long id);
}

package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.CourseReview;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {

    @EntityGraph(attributePaths = {"student", "course"})
    List<CourseReview> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    @EntityGraph(attributePaths = {"student", "course"})
    Page<CourseReview> findByCourseId(Long courseId, Pageable pageable);

    Optional<CourseReview> findByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    long countByCourseId(Long courseId);

    @Query("select avg(r.rating) from CourseReview r where r.course.id = :courseId")
    Double findAverageRating(@Param("courseId") Long courseId);
}

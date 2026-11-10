package ru.alexey.coursehub.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByLessonIdOrderByDueDateAsc(Long lessonId);

    long countByLessonId(Long lessonId);

    @Query("select a from Assignment a join fetch a.lesson l join fetch l.module m where a.id = :id")
    Optional<Assignment> findByIdWithLesson(@Param("id") Long id);

    @Query("select a from Assignment a where a.lesson.id = :lessonId and a.dueDate < :dueDate")
    List<Assignment> findOverdueByLesson(@Param("lessonId") Long lessonId, @Param("dueDate") LocalDateTime dueDate);
}

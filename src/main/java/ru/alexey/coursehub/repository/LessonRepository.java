package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByModuleIdOrderByIdAsc(Long moduleId);

    long countByModuleId(Long moduleId);

    @Query("select l from Lesson l join fetch l.assignments where l.id = :id")
    Optional<Lesson> findByIdWithAssignments(@Param("id") Long id);

    @Query("select l from Lesson l join fetch l.module m where l.id = :id")
    Optional<Lesson> findByIdWithModule(@Param("id") Long id);
}

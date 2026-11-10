package ru.alexey.coursehub.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByModuleId(Long moduleId);

    boolean existsByModuleId(Long moduleId);

    @Query("select q from Quiz q join fetch q.questions where q.id = :id")
    Optional<Quiz> findByIdWithQuestions(@Param("id") Long id);

    @Query("select q from Quiz q join fetch q.module m join fetch m.course where q.id = :id")
    Optional<Quiz> findByIdWithModule(@Param("id") Long id);
}

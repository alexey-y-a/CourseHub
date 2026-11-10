package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuizIdOrderByIdAsc(Long quizId);

    long countByQuizId(Long quizId);

    @EntityGraph(attributePaths = {"options"})
    List<Question> findByQuizId(Long quizId);

    @Query("select q from Question q join fetch q.options where q.id = :id")
    Optional<Question> findByIdWithOptions(@Param("id") Long id);
}

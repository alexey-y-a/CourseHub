package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.QuizSubmission;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {

    List<QuizSubmission> findByStudentIdOrderByTakenAtDesc(Long studentId);

    List<QuizSubmission> findByQuizId(Long quizId);

    boolean existsByQuizIdAndStudentId(Long quizId, Long studentId);

    boolean existsByStudentId(Long studentId);

    long countByQuizId(Long quizId);

    long countByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"answers", "answers.selectedOption", "answers.question"})
    @Query("select qs from QuizSubmission qs where qs.id = :id")
    Optional<QuizSubmission> findWithAnswers(@Param("id") Long id);

    @Query("""
            select qs from QuizSubmission qs
            join fetch qs.quiz q
            join fetch q.module m
            where m.course.id = :courseId
            """)
    List<QuizSubmission> findByCourseId(@Param("courseId") Long courseId);

    @Query("select avg(qs.score) from QuizSubmission qs where qs.quiz.id = :quizId")
    Double findAverageScore(@Param("quizId") Long quizId);
}

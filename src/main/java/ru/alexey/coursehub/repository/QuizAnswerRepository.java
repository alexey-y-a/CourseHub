package ru.alexey.coursehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexey.coursehub.entity.QuizAnswer;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {

    List<QuizAnswer> findByQuizSubmissionId(Long quizSubmissionId);

    boolean existsByQuizSubmissionIdAndQuestionId(Long quizSubmissionId, Long questionId);

    long countByQuizSubmissionIdAndSelectedOptionCorrectTrue(Long quizSubmissionId);
}

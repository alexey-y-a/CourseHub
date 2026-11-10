package ru.alexey.coursehub.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexey.coursehub.entity.AnswerOption;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {

    List<AnswerOption> findByQuestionIdOrderByIdAsc(Long questionId);

    List<AnswerOption> findByQuestionIdAndCorrectTrue(Long questionId);

    List<AnswerOption> findByQuestionQuizId(Long quizId);

    long countByQuestionId(Long questionId);

    long countByQuestionIdAndCorrectTrue(Long questionId);
}

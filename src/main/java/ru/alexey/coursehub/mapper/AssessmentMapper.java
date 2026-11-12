package ru.alexey.coursehub.mapper;

import java.util.Comparator;
import ru.alexey.coursehub.dto.response.OptionResponse;
import ru.alexey.coursehub.dto.response.QuestionResponse;
import ru.alexey.coursehub.dto.response.QuizResponse;
import ru.alexey.coursehub.dto.response.QuizResultResponse;
import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.QuizSubmission;

public final class AssessmentMapper {

    private AssessmentMapper() {}

    public static QuizResponse toResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getModule().getId(),
                quiz.getTitle(),
                quiz.getTimeLimit(),
                quiz.getQuestions().stream()
                        .sorted(Comparator.comparing(Question::getId))
                        .map(AssessmentMapper::toResponse)
                        .toList());
    }

    public static QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getType(),
                question.getOptions().stream()
                        .sorted(Comparator.comparing(AnswerOption::getId))
                        .map(option -> new OptionResponse(option.getId(), option.getText()))
                        .toList());
    }

    public static QuizResultResponse toResponse(QuizSubmission submission) {
        return new QuizResultResponse(
                submission.getId(),
                submission.getQuiz().getId(),
                submission.getQuiz().getTitle(),
                submission.getStudent().getId(),
                submission.getStudent().getName(),
                submission.getScore(),
                submission.getTakenAt());
    }
}

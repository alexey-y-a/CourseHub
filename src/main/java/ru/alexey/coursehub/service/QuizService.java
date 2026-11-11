package ru.alexey.coursehub.service;

import java.util.List;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.QuizSubmission;

public interface QuizService {

    Quiz create(Long moduleId, CreateQuizRequest request);

    Quiz get(Long id);

    Quiz getWithQuestions(Long id);

    QuizSubmission submit(Long quizId, Long studentId, SubmitQuizRequest request);

    List<QuizSubmission> resultsByStudent(Long studentId);

    List<QuizSubmission> resultsByCourse(Long courseId);

    Double averageScore(Long quizId);
}

package ru.alexey.coursehub.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.CreateOptionRequest;
import ru.alexey.coursehub.dto.request.CreateQuestionRequest;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.QuizSubmission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.QuestionType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.repository.ModuleRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.QuizService;
import ru.alexey.coursehub.support.TestData;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizServiceIT {

    @Autowired
    private QuizService quizService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    private Module module;
    private User student;

    @BeforeEach
    void setUp() {
        Category category = categoryRepository.save(TestData.category("Programming"));
        User teacher = userRepository.save(TestData.user("Ivan", "teacher@example.com", Role.TEACHER));
        student = userRepository.save(TestData.user("Anna", "student@example.com", Role.STUDENT));
        Course course = courseRepository.save(TestData.course("Hibernate", category, teacher));
        module = moduleRepository.save(TestData.module(course, "Intro", 1));
    }

    @Test
    void createsQuizAndScoresPerfectAttempt() {
        Quiz quiz = quizService.create(
                module.getId(),
                new CreateQuizRequest(
                        "Quiz 1",
                        10,
                        List.of(new CreateQuestionRequest(
                                "2 + 2?",
                                QuestionType.SINGLE_CHOICE,
                                List.of(new CreateOptionRequest("4", true), new CreateOptionRequest("5", false))))));
        Question question = quiz.getQuestions().get(0);
        AnswerOption correct = question.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .findFirst()
                .orElseThrow();

        QuizSubmission submission = quizService.submit(
                quiz.getId(),
                student.getId(),
                new SubmitQuizRequest(Map.of(question.getId(), List.of(correct.getId()))));

        assertThat(submission.getScore()).isEqualTo(100);
        assertThat(quizService.resultsByStudent(student.getId())).hasSize(1);
        assertThat(quizService.averageScore(quiz.getId())).isEqualTo(100.0);
    }

    @Test
    void blocksSecondAttemptOnSameQuiz() {
        Quiz quiz = quizService.create(
                module.getId(),
                new CreateQuizRequest(
                        "Quiz 1",
                        10,
                        List.of(new CreateQuestionRequest(
                                "2 + 2?",
                                QuestionType.SINGLE_CHOICE,
                                List.of(new CreateOptionRequest("4", true), new CreateOptionRequest("5", false))))));
        Question question = quiz.getQuestions().get(0);
        AnswerOption correct = question.getOptions().stream()
                .filter(AnswerOption::isCorrect)
                .findFirst()
                .orElseThrow();
        SubmitQuizRequest request = new SubmitQuizRequest(Map.of(question.getId(), List.of(correct.getId())));

        quizService.submit(quiz.getId(), student.getId(), request);

        assertThatThrownBy(() -> quizService.submit(quiz.getId(), student.getId(), request))
                .isInstanceOf(ConflictException.class);
    }
}

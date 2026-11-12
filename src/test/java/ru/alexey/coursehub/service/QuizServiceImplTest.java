package ru.alexey.coursehub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alexey.coursehub.dto.request.CreateOptionRequest;
import ru.alexey.coursehub.dto.request.CreateQuestionRequest;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.QuizSubmission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.QuestionType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.repository.ModuleRepository;
import ru.alexey.coursehub.repository.QuizRepository;
import ru.alexey.coursehub.repository.QuizSubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.impl.QuizServiceImpl;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizSubmissionRepository quizSubmissionRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private QuizServiceImpl quizService;

    private Quiz quiz;
    private Question question;
    private AnswerOption correctOption;
    private AnswerOption wrongOption;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setId(10L);
        question.setType(QuestionType.SINGLE_CHOICE);
        question.setText("2 + 2?");
        correctOption = new AnswerOption();
        correctOption.setId(100L);
        correctOption.setText("4");
        correctOption.setCorrect(true);
        correctOption.setQuestion(question);
        wrongOption = new AnswerOption();
        wrongOption.setId(101L);
        wrongOption.setText("5");
        wrongOption.setCorrect(false);
        wrongOption.setQuestion(question);
        question.getOptions().add(correctOption);
        question.getOptions().add(wrongOption);
        quiz = new Quiz();
        quiz.setId(1L);
        quiz.setTitle("Quiz");
        quiz.getQuestions().add(question);
        question.setQuiz(quiz);
    }

    @Test
    void scoresPerfectAttemptAsHundred() {
        stubAttempt();
        QuizSubmission submission = quizService.submit(1L, 2L, new SubmitQuizRequest(Map.of(10L, List.of(100L))));

        assertThat(submission.getScore()).isEqualTo(100);
        assertThat(submission.getAnswers()).hasSize(1);
        assertThat(submission.getAnswers().get(0).getSelectedOption()).isSameAs(correctOption);
    }

    @Test
    void scoresWrongAnswerAsZero() {
        stubAttempt();
        QuizSubmission submission = quizService.submit(1L, 2L, new SubmitQuizRequest(Map.of(10L, List.of(101L))));

        assertThat(submission.getScore()).isZero();
    }

    @Test
    void rejectsMultipleAnswersForSingleChoiceQuestion() {
        when(quizRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));
        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> quizService.submit(1L, 2L, new SubmitQuizRequest(Map.of(10L, List.of(100L, 101L)))))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("only one option");
    }

    @Test
    void rejectsNotAnsweredQuestion() {
        when(quizRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));
        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> quizService.submit(1L, 2L, new SubmitQuizRequest(Map.of(999L, List.of(100L)))))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsSingleChoiceQuestionWithTwoCorrectOptions() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(new ru.alexey.coursehub.entity.Module()));
        when(quizRepository.existsByModuleId(1L)).thenReturn(false);

        CreateQuizRequest request = new CreateQuizRequest(
                "Quiz",
                10,
                List.of(new CreateQuestionRequest(
                        "2 + 2?",
                        QuestionType.SINGLE_CHOICE,
                        List.of(new CreateOptionRequest("4", true), new CreateOptionRequest("5", true)))));

        assertThatThrownBy(() -> quizService.create(1L, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exactly one correct");
    }

    private void stubAttempt() {
        when(quizRepository.findByIdWithQuestions(1L)).thenReturn(Optional.of(quiz));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student()));
        when(quizSubmissionRepository.existsByQuizIdAndStudentId(1L, 2L)).thenReturn(false);
        when(quizSubmissionRepository.save(any(QuizSubmission.class))).thenAnswer(invocation -> {
            QuizSubmission submission = invocation.getArgument(0);
            submission.setId(500L);
            return submission;
        });
    }

    private User student() {
        User student = new User();
        student.setId(2L);
        student.setName("Anna");
        student.setRole(Role.STUDENT);
        return student;
    }
}

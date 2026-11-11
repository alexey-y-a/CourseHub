package ru.alexey.coursehub.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.CreateOptionRequest;
import ru.alexey.coursehub.dto.request.CreateQuestionRequest;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.QuizAnswer;
import ru.alexey.coursehub.entity.QuizSubmission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.NotificationType;
import ru.alexey.coursehub.entity.enums.QuestionType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.exception.BadRequestException;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.ModuleRepository;
import ru.alexey.coursehub.repository.QuizRepository;
import ru.alexey.coursehub.repository.QuizSubmissionRepository;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.NotificationService;
import ru.alexey.coursehub.service.QuizService;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public Quiz create(Long moduleId, CreateQuizRequest request) {
        Module module = moduleRepository.findById(moduleId).orElseThrow(() -> NotFoundException.of("Module", moduleId));
        if (quizRepository.existsByModuleId(moduleId)) {
            throw new ConflictException("Module " + moduleId + " already has a quiz");
        }
        Quiz quiz = new Quiz();
        quiz.setModule(module);
        quiz.setTitle(request.title().trim());
        quiz.setTimeLimit(request.timeLimit());
        for (CreateQuestionRequest questionRequest : request.questions()) {
            validateQuestion(questionRequest);
            Question question = new Question();
            question.setQuiz(quiz);
            question.setText(questionRequest.text().trim());
            question.setType(questionRequest.type());
            for (CreateOptionRequest optionRequest : questionRequest.options()) {
                AnswerOption option = new AnswerOption();
                option.setQuestion(question);
                option.setText(optionRequest.text().trim());
                option.setCorrect(optionRequest.correct());
                question.getOptions().add(option);
            }
            quiz.getQuestions().add(question);
        }
        Quiz saved = quizRepository.save(quiz);
        log.info(
                "Created quiz {} with {} questions for module {}",
                saved.getId(),
                saved.getQuestions().size(),
                moduleId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Quiz get(Long id) {
        return quizRepository.findByIdWithModule(id).orElseThrow(() -> NotFoundException.of("Quiz", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Quiz getWithQuestions(Long id) {
        Quiz quiz = quizRepository.findByIdWithQuestions(id).orElseThrow(() -> NotFoundException.of("Quiz", id));
        quiz.getQuestions().forEach(question -> question.getOptions().size());
        return quiz;
    }

    @Override
    @Transactional
    public QuizSubmission submit(Long quizId, Long studentId, SubmitQuizRequest request) {
        Quiz quiz =
                quizRepository.findByIdWithQuestions(quizId).orElseThrow(() -> NotFoundException.of("Quiz", quizId));
        User student = requireStudent(studentId);
        if (quizSubmissionRepository.existsByQuizIdAndStudentId(quizId, studentId)) {
            throw new ConflictException("Student " + studentId + " has already passed quiz " + quizId);
        }
        List<Question> questions = quiz.getQuestions();
        if (questions.isEmpty()) {
            throw new BadRequestException("Quiz " + quizId + " has no questions");
        }
        Map<Long, List<Long>> answers = request.answers();
        if (answers.size() != questions.size()) {
            throw new BadRequestException("All questions must be answered");
        }
        QuizSubmission submission = new QuizSubmission();
        submission.setQuiz(quiz);
        submission.setStudent(student);
        submission.setTakenAt(LocalDateTime.now());
        int correctAnswers = 0;
        for (Question question : questions) {
            List<Long> selected = answers.get(question.getId());
            if (selected == null || selected.isEmpty()) {
                throw new BadRequestException("Question " + question.getId() + " is not answered");
            }
            Set<Long> selectedIds = new HashSet<>(selected);
            if (question.getType() == QuestionType.SINGLE_CHOICE && selectedIds.size() != 1) {
                throw new BadRequestException("Question " + question.getId() + " accepts only one option");
            }
            Set<Long> correctIds = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .collect(Collectors.toSet());
            for (Long optionId : selectedIds) {
                AnswerOption option = question.getOptions().stream()
                        .filter(candidate -> candidate.getId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException(
                                "Option " + optionId + " does not belong to question " + question.getId()));
                QuizAnswer answer = new QuizAnswer();
                answer.setQuizSubmission(submission);
                answer.setQuestion(question);
                answer.setSelectedOption(option);
                submission.getAnswers().add(answer);
            }
            if (correctIds.equals(selectedIds)) {
                correctAnswers++;
            }
        }
        submission.setScore((int) Math.round(100.0 * correctAnswers / questions.size()));
        QuizSubmission saved = quizSubmissionRepository.save(submission);
        notificationService.notify(
                studentId,
                NotificationType.QUIZ,
                "Quiz " + quiz.getTitle() + " completed with score " + saved.getScore());
        log.info("Student {} passed quiz {} with score {}", studentId, quizId, saved.getScore());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSubmission> resultsByStudent(Long studentId) {
        return quizSubmissionRepository.findByStudentIdOrderByTakenAtDesc(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSubmission> resultsByCourse(Long courseId) {
        return quizSubmissionRepository.findByCourseId(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Double averageScore(Long quizId) {
        return quizSubmissionRepository.findAverageScore(quizId);
    }

    private void validateQuestion(CreateQuestionRequest request) {
        if (request.options().size() < 2) {
            throw new BadRequestException("Question must have at least two options");
        }
        long correct =
                request.options().stream().filter(CreateOptionRequest::correct).count();
        if (correct == 0) {
            throw new BadRequestException("Question must have at least one correct option");
        }
        if (request.type() == QuestionType.SINGLE_CHOICE && correct != 1) {
            throw new BadRequestException("Single choice question must have exactly one correct option");
        }
    }

    private User requireStudent(Long studentId) {
        User user = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.of("User", studentId));
        if (user.getRole() != Role.STUDENT) {
            throw new BadRequestException("User " + studentId + " is not a student");
        }
        return user;
    }
}

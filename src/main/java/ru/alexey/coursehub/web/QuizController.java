package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.dto.response.AverageResponse;
import ru.alexey.coursehub.dto.response.QuizResponse;
import ru.alexey.coursehub.dto.response.QuizResultResponse;
import ru.alexey.coursehub.mapper.AssessmentMapper;
import ru.alexey.coursehub.service.QuizService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/api/modules/{moduleId}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create quiz for module")
    public QuizResponse create(@PathVariable Long moduleId, @Valid @RequestBody CreateQuizRequest request) {
        return AssessmentMapper.toResponse(quizService.create(moduleId, request));
    }

    @GetMapping("/api/quizzes/{id}")
    @Operation(summary = "Get quiz with questions and options without correct flags")
    public QuizResponse get(@PathVariable Long id) {
        return AssessmentMapper.toResponse(quizService.getWithQuestions(id));
    }

    @PostMapping("/api/quizzes/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Pass quiz and get score")
    public QuizResultResponse submit(
            @PathVariable Long id, @RequestParam Long studentId, @Valid @RequestBody SubmitQuizRequest request) {
        return AssessmentMapper.toResponse(quizService.submit(id, studentId, request));
    }

    @GetMapping("/api/users/{studentId}/quiz-results")
    @Operation(summary = "List quiz results of student")
    public List<QuizResultResponse> resultsByStudent(@PathVariable Long studentId) {
        return quizService.resultsByStudent(studentId).stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/courses/{courseId}/quiz-results")
    @Operation(summary = "List quiz results in course")
    public List<QuizResultResponse> resultsByCourse(@PathVariable Long courseId) {
        return quizService.resultsByCourse(courseId).stream()
                .map(AssessmentMapper::toResponse)
                .toList();
    }

    @GetMapping("/api/quizzes/{id}/average")
    @Operation(summary = "Average score of quiz")
    public AverageResponse average(@PathVariable Long id) {
        return new AverageResponse(quizService.averageScore(id));
    }
}

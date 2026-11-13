package ru.alexey.coursehub.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.alexey.coursehub.dto.request.CategoryRequest;
import ru.alexey.coursehub.dto.request.CreateAssignmentRequest;
import ru.alexey.coursehub.dto.request.CreateCourseRequest;
import ru.alexey.coursehub.dto.request.CreateLessonRequest;
import ru.alexey.coursehub.dto.request.CreateModuleRequest;
import ru.alexey.coursehub.dto.request.CreateOptionRequest;
import ru.alexey.coursehub.dto.request.CreateQuestionRequest;
import ru.alexey.coursehub.dto.request.CreateQuizRequest;
import ru.alexey.coursehub.dto.request.CreateUserRequest;
import ru.alexey.coursehub.dto.request.GradeSubmissionRequest;
import ru.alexey.coursehub.dto.request.ReviewRequest;
import ru.alexey.coursehub.dto.request.SubmitAssignmentRequest;
import ru.alexey.coursehub.dto.request.SubmitQuizRequest;
import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.Submission;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.QuestionType;
import ru.alexey.coursehub.entity.enums.Role;
import ru.alexey.coursehub.repository.UserRepository;
import ru.alexey.coursehub.service.AssignmentService;
import ru.alexey.coursehub.service.CategoryService;
import ru.alexey.coursehub.service.CourseService;
import ru.alexey.coursehub.service.EnrollmentService;
import ru.alexey.coursehub.service.QuizService;
import ru.alexey.coursehub.service.ReviewService;
import ru.alexey.coursehub.service.SubmissionService;
import ru.alexey.coursehub.service.UserService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "coursehub.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;
    private final QuizService quizService;
    private final ReviewService reviewService;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Demo data already present, skipping initializer");
            return;
        }

        Category programming = categoryService.create(new CategoryRequest("Программирование"));
        Category databases = categoryService.create(new CategoryRequest("Базы данных"));
        Category design = categoryService.create(new CategoryRequest("Дизайн"));

        User admin = userService.create(new CreateUserRequest("Администратор", "admin@coursehub.example", Role.ADMIN));
        User ivan = userService.create(new CreateUserRequest("Иван Петров", "ivan@coursehub.example", Role.TEACHER));
        User maria = userService.create(new CreateUserRequest("Мария Соколова", "maria@coursehub.example", Role.TEACHER));
        User anna = userService.create(new CreateUserRequest("Анна Смирнова", "anna@coursehub.example", Role.STUDENT));
        User petr = userService.create(new CreateUserRequest("Пётр Кузнецов", "petr@coursehub.example", Role.STUDENT));
        User olga = userService.create(new CreateUserRequest("Ольга Иванова", "olga@coursehub.example", Role.STUDENT));

        Course hibernate = courseService.create(new CreateCourseRequest(
                "Hibernate и JPA",
                "Практический курс по ORM: сущности, связи, ленивая загрузка и запросы.",
                programming.getId(),
                ivan.getId(),
                24,
                LocalDate.now().minusDays(10),
                Set.of("Java", "Hibernate", "JPA")));

        Course springData = courseService.create(new CreateCourseRequest(
                "Spring Data JPA",
                "Репозитории, производные запросы, пагинация и спецификации.",
                programming.getId(),
                ivan.getId(),
                16,
                LocalDate.now().minusDays(3),
                Set.of("Java", "Spring")));

        Course databaseDesign = courseService.create(new CreateCourseRequest(
                "Проектирование баз данных",
                "Нормализация, индексы, транзакции и PostgreSQL.",
                databases.getId(),
                maria.getId(),
                20,
                LocalDate.now().plusDays(5),
                Set.of("SQL", "PostgreSQL")));

        Module intro = courseService.addModule(
                hibernate.getId(),
                new CreateModuleRequest("Введение в ORM", "Зачем нужен ORM и как устроен Hibernate", 1));
        Module relations = courseService.addModule(
                hibernate.getId(),
                new CreateModuleRequest("Связи сущностей", "Один к одному, один ко многим, многие ко многим", 2));
        Module springModule = courseService.addModule(
                springData.getId(), new CreateModuleRequest("Репозитории", "Spring Data JPA в действии", 1));
        Module normalization = courseService.addModule(
                databaseDesign.getId(), new CreateModuleRequest("Нормальные формы", "От 1NF к 3NF", 1));

        Lesson whatIsOrm = courseService.addLesson(
                intro.getId(),
                new CreateLessonRequest(
                        "Что такое ORM",
                        "ORM отображает объектную модель на реляционную схему и берёт на себя генерацию SQL.",
                        "https://example.com/video/what-is-orm"));
        Lesson lazyLoading = courseService.addLesson(
                relations.getId(),
                new CreateLessonRequest(
                        "Ленивая загрузка",
                        "Разбираем FetchType.LAZY, прокси и LazyInitializationException.",
                        "https://example.com/video/lazy-loading"));
        courseService.addLesson(
                springModule.getId(),
                new CreateLessonRequest("Производные запросы", "findByTitleContainingIgnoreCase и другие.", null));
        courseService.addLesson(
                normalization.getId(),
                new CreateLessonRequest("Первая нормальная форма", "Атомарность значений.", null));

        Assignment homework = assignmentService.create(
                whatIsOrm.getId(),
                new CreateAssignmentRequest(
                        "Домашнее задание 1",
                        "Описать маппинг трёх сущностей и связи между ними.",
                        LocalDateTime.now().plusDays(7),
                        100));
        assignmentService.create(
                lazyLoading.getId(),
                new CreateAssignmentRequest(
                        "Домашнее задание 2",
                        "Найти и исправить N+1 в предложенном коде.",
                        LocalDateTime.now().plusDays(10),
                        50));

        enrollmentService.enroll(hibernate.getId(), anna.getId());
        enrollmentService.enroll(hibernate.getId(), petr.getId());
        enrollmentService.enroll(springData.getId(), anna.getId());
        enrollmentService.enroll(databaseDesign.getId(), olga.getId());

        Submission annaSubmission =
                submissionService.submit(homework.getId(), anna.getId(), new SubmitAssignmentRequest(
                        "Customer 1-1 Profile, Course 1-M Module, Course M-M Tag через course_tag."));
        submissionService.grade(annaSubmission.getId(), new GradeSubmissionRequest(90, "Хорошо, но не хватает примера кода."));

        Submission petrSubmission =
                submissionService.submit(homework.getId(), petr.getId(), new SubmitAssignmentRequest(
                        "Сущности выделены, связи описаны, каскады не указаны."));
        submissionService.grade(petrSubmission.getId(), new GradeSubmissionRequest(75, "Добавьте стратегии каскадов."));

        Quiz quiz = quizService.create(
                intro.getId(),
                new CreateQuizRequest(
                        "Тест по модулю 1",
                        15,
                        List.of(
                                new CreateQuestionRequest(
                                        "Что делает аннотация @Entity?",
                                        QuestionType.SINGLE_CHOICE,
                                        List.of(
                                                new CreateOptionRequest("Отмечает класс как сущность JPA", true),
                                                new CreateOptionRequest("Задаёт имя таблицы", false),
                                                new CreateOptionRequest("Включает кеширование", false))),
                                new CreateQuestionRequest(
                                        "Какие типы связей поддерживает JPA?",
                                        QuestionType.MULTIPLE_CHOICE,
                                        List.of(
                                                new CreateOptionRequest("OneToOne", true),
                                                new CreateOptionRequest("ManyToMany", true),
                                                new CreateOptionRequest("ManyToOne", true),
                                                new CreateOptionRequest("TableToTable", false))))));

        Map<Long, List<Long>> answers = new HashMap<>();
        for (Question question : quiz.getQuestions()) {
            List<Long> correct = question.getOptions().stream()
                    .filter(AnswerOption::isCorrect)
                    .map(AnswerOption::getId)
                    .toList();
            answers.put(question.getId(), correct);
        }
        quizService.submit(quiz.getId(), anna.getId(), new SubmitQuizRequest(answers));

        reviewService.add(hibernate.getId(), anna.getId(), new ReviewRequest(5, "Понятно и по делу."));
        reviewService.add(hibernate.getId(), petr.getId(), new ReviewRequest(4, "Хотелось бы больше практики."));

        log.info(
                "Demo data created: categories={}, users={}, courses={}, quiz={}",
                List.of(programming, databases, design).size(),
                List.of(admin, ivan, maria, anna, petr, olga).size(),
                List.of(hibernate, springData, databaseDesign).size(),
                quiz.getId());
    }
}

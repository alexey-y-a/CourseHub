package ru.alexey.coursehub.support;

import ru.alexey.coursehub.entity.AnswerOption;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.entity.Course;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.entity.Module;
import ru.alexey.coursehub.entity.Question;
import ru.alexey.coursehub.entity.Quiz;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.QuestionType;
import ru.alexey.coursehub.entity.enums.Role;

public final class TestData {

    private TestData() {}

    public static Category category(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    public static User user(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    public static Course course(String title, Category category, User teacher) {
        Course course = new Course();
        course.setTitle(title);
        course.setDescription("Description of " + title);
        course.setCategory(category);
        course.setTeacher(teacher);
        course.setDuration(10);
        return course;
    }

    public static Module module(Course course, String title, int orderIndex) {
        Module module = new Module();
        module.setCourse(course);
        module.setTitle(title);
        module.setOrderIndex(orderIndex);
        return module;
    }

    public static Lesson lesson(Module module, String title) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setContent("Content of " + title);
        return lesson;
    }

    public static Assignment assignment(Lesson lesson, String title, int maxScore) {
        Assignment assignment = new Assignment();
        assignment.setLesson(lesson);
        assignment.setTitle(title);
        assignment.setDescription("Do " + title);
        assignment.setMaxScore(maxScore);
        return assignment;
    }

    public static Quiz quiz(Module module, String title, boolean correctAnswer) {
        Quiz quiz = new Quiz();
        quiz.setModule(module);
        quiz.setTitle(title);
        quiz.setTimeLimit(15);
        Question question = new Question();
        question.setQuiz(quiz);
        question.setText("Question of " + title);
        question.setType(QuestionType.SINGLE_CHOICE);
        AnswerOption right = new AnswerOption();
        right.setQuestion(question);
        right.setText("right");
        right.setCorrect(true);
        AnswerOption wrong = new AnswerOption();
        wrong.setQuestion(question);
        wrong.setText("wrong");
        wrong.setCorrect(false);
        question.getOptions().add(right);
        question.getOptions().add(wrong);
        quiz.getQuestions().add(question);
        return quiz;
    }
}

# Модель данных

18 таблиц: 17 сущностей и одна таблица связи `course_tag`.

## 1. Сущности

| Сущность | Таблица | Ключевые поля |
|---|---|---|
| User | `users` | name, email (unique), role |
| Profile | `profiles` | user_id (unique), bio, avatar_url, contacts |
| Category | `categories` | name (unique) |
| Course | `courses` | title, description, category_id, teacher_id, duration, start_date |
| Tag | `tags` | name (unique) |
| Module | `modules` | course_id, title, order_index, description |
| Lesson | `lessons` | module_id, title, content, video_url |
| Assignment | `assignments` | lesson_id, title, description, due_date, max_score |
| Submission | `submissions` | assignment_id, student_id, submitted_at, content, score, feedback, status |
| Enrollment | `enrollments` | course_id, student_id, enroll_date, status |
| Quiz | `quizzes` | module_id (unique), title, time_limit |
| Question | `questions` | quiz_id, text, type |
| AnswerOption | `answer_options` | question_id, text, is_correct |
| QuizSubmission | `quiz_submissions` | quiz_id, student_id, score, taken_at |
| QuizAnswer | `quiz_answers` | quiz_submission_id, question_id, selected_option_id |
| CourseReview | `course_reviews` | course_id, student_id, rating, comment |
| Notification | `notifications` | user_id, message, type, is_read |

`course_tag` содержит `course_id` и `tag_id`, первичный ключ составной.

## 2. Связи

| Связь | Тип | Реализация |
|---|---|---|
| User - Profile | 1-1 | `profiles.user_id` unique, владелец Profile |
| Module - Quiz | 1-1 | `quizzes.module_id` unique, владелец Quiz |
| User (teacher) - Course | 1-M | `courses.teacher_id` |
| Category - Course | 1-M | `courses.category_id` |
| Course - Module - Lesson - Assignment | 1-M | по цепочке FK |
| Quiz - Question - AnswerOption | 1-M | по цепочке FK |
| Course - Tag | M-M | `course_tag` |
| User - Course | M-M через Enrollment | `enrollments` с полями и статусом |
| User - Assignment | M-M через Submission | `submissions` с оценкой и комментарием |
| User - Quiz | M-M через QuizSubmission | `quiz_submissions` |
| QuizSubmission - AnswerOption | M-M через QuizAnswer | выбранные варианты попытки |
| User - Course (отзывы) | M-M через CourseReview | рейтинг и комментарий |

## 3. Ограничения целостности

Уникальность:

- `users.email`
- `categories.name`, `tags.name`
- `profiles.user_id`
- `quizzes.module_id`
- `enrollments(course_id, student_id)`
- `submissions(assignment_id, student_id)`
- `course_reviews(course_id, student_id)`

Проверки (`CHECK`):

- `assignments.max_score > 0`
- `quizzes.time_limit > 0`
- `course_reviews.rating between 1 and 5`
- значения перечислений (генерируются Hibernate)

Индексы: внешние ключи, `notifications(user_id, is_read)`,
`quiz_answers(quiz_submission_id)`, `quiz_answers(selected_option_id)`.

## 4. Каскады и удаление

| Родитель | Дочерние | Поведение |
|---|---|---|
| Course | Module, Lesson, Assignment, CourseReview | каскадное удаление |
| Module | Lesson, Quiz | каскадное удаление |
| Quiz | Question, AnswerOption | каскадное удаление |
| Question | AnswerOption | каскадное удаление |
| QuizSubmission | QuizAnswer | каскадное удаление |
| User | Profile | каскадное удаление |
| Course | Enrollment | запрет удаления, если есть записи (409) |
| Assignment | Submission | запрет удаления, если есть решения (409) |

Остальные связи без каскада: историю обучения нельзя терять молча.

## 5. Схема и миграции

Схема создаётся Hibernate из сущностей:

- `dev`: `ddl-auto: update`
- `test`: `ddl-auto: create-drop`
- `prod`-профиля нет, обоснование в [ADR 001](adr/001-ddl-auto.md)

Пересоздать схему в разработке:

```bash
docker exec coursehub-db psql -U coursehub -d coursehub -c 'drop schema public cascade; create schema public;'
```

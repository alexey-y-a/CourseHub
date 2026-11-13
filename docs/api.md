# API

Базовый путь: `/api`. Формат: JSON. Документация в рантайме: `/swagger-ui/index.html`.

## 1. Общие правила

- Пагинация: `page`, `size`, `sort`; ответ содержит `content`, `page`, `size`,
  `totalElements`, `totalPages`, `first`, `last`.
- Ошибки: `application/problem+json` с полями `type`, `title`, `status`, `detail`,
  `instance`, `code`, `timestamp`; при валидации добавляется `errors[].field` и `errors[].message`.
- Коды: `201` при создании, `204` при удалении, `200` в остальных случаях.

## 2. Пользователи

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/users` | создать пользователя |
| GET | `/api/users/{id}` | пользователь с профилем |
| GET | `/api/users?role=&search=&page=` | список с фильтрами |
| PUT | `/api/users/{id}` | частичное обновление |
| PUT | `/api/users/{id}/profile` | создать или обновить профиль |
| DELETE | `/api/users/{id}` | удалить |

## 3. Категории

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/categories` | создать |
| GET | `/api/categories` | список по алфавиту |
| GET | `/api/categories/{id}` | одна категория |
| PUT | `/api/categories/{id}` | переименовать |
| DELETE | `/api/categories/{id}` | удалить, если нет курсов |

## 4. Курсы, модули, уроки

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/courses` | создать курс с категорией, преподавателем и тегами |
| GET | `/api/courses?categoryId=&teacherId=&title=&page=` | список с фильтрами |
| GET | `/api/courses/{id}` | курс с тегами |
| GET | `/api/courses/{id}/structure` | модули и уроки |
| PUT | `/api/courses/{id}` | обновить |
| DELETE | `/api/courses/{id}` | удалить, если нет записей |
| POST | `/api/courses/{id}/modules` | добавить модуль |
| GET | `/api/courses/{id}/modules` | модули курса |
| PUT | `/api/modules/{id}` | обновить модуль |
| DELETE | `/api/modules/{id}` | удалить модуль с уроками |
| POST | `/api/modules/{id}/lessons` | добавить урок |
| GET | `/api/modules/{id}/lessons` | уроки модуля |
| PUT | `/api/lessons/{id}` | обновить урок |
| DELETE | `/api/lessons/{id}` | удалить урок с заданиями |

## 5. Запись на курс

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/courses/{id}/enrollments?studentId=` | записать студента |
| GET | `/api/courses/{id}/enrollments` | студенты курса |
| DELETE | `/api/courses/{id}/enrollments/{studentId}` | отписать |
| GET | `/api/users/{id}/enrollments?status=&page=` | курсы студента |
| GET | `/api/enrollments/{id}` | одна запись |
| PATCH | `/api/enrollments/{id}` | сменить статус |
| DELETE | `/api/enrollments/{id}` | удалить запись |

## 6. Задания и решения

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/lessons/{id}/assignments` | создать задание |
| GET | `/api/lessons/{id}/assignments` | задания урока |
| GET | `/api/assignments/{id}` | задание |
| PUT | `/api/assignments/{id}` | обновить |
| DELETE | `/api/assignments/{id}` | удалить, если нет решений |
| POST | `/api/assignments/{id}/submissions?studentId=` | сдать решение |
| GET | `/api/assignments/{id}/submissions` | решения задания |
| GET | `/api/submissions/{id}` | решение |
| PATCH | `/api/submissions/{id}/grade` | выставить оценку |
| GET | `/api/users/{id}/submissions?page=` | решения студента |
| GET | `/api/users/{id}/courses/{courseId}/submissions` | решения студента в курсе |

## 7. Тесты

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/modules/{id}/quiz` | создать тест модуля |
| GET | `/api/quizzes/{id}` | тест без правильных ответов |
| POST | `/api/quizzes/{id}/submissions?studentId=` | пройти тест |
| GET | `/api/users/{id}/quiz-results` | результаты студента |
| GET | `/api/courses/{id}/quiz-results` | результаты по курсу |
| GET | `/api/quizzes/{id}/average` | средний балл |

## 8. Отзывы и уведомления

| Метод | Путь | Назначение |
|---|---|---|
| POST | `/api/courses/{id}/reviews?studentId=` | оставить отзыв |
| GET | `/api/courses/{id}/reviews?page=` | отзывы курса |
| GET | `/api/courses/{id}/rating` | средний рейтинг |
| DELETE | `/api/reviews/{id}` | удалить отзыв |
| GET | `/api/users/{id}/notifications` | уведомления |
| GET | `/api/users/{id}/notifications/unread` | непрочитанные |
| GET | `/api/users/{id}/notifications/unread-count` | счётчик |
| POST | `/api/users/{id}/notifications/read` | отметить прочитанными |

## 9. Пример сценария

```bash
H=http://localhost:8080

CAT=$(curl -s -X POST $H/api/categories -H 'Content-Type: application/json' \
  -d '{"name":"Programming"}' | jq -r .id)

T=$(curl -s -X POST $H/api/users -H 'Content-Type: application/json' \
  -d '{"name":"Ivan","email":"ivan@example.com","role":"TEACHER"}' | jq -r .id)
S=$(curl -s -X POST $H/api/users -H 'Content-Type: application/json' \
  -d '{"name":"Anna","email":"anna@example.com","role":"STUDENT"}' | jq -r .id)

C=$(curl -s -X POST $H/api/courses -H 'Content-Type: application/json' \
  -d "{\"title\":\"Hibernate\",\"categoryId\":$CAT,\"teacherId\":$T,\"tags\":[\"Java\"]}" | jq -r .id)

M=$(curl -s -X POST $H/api/courses/$C/modules -H 'Content-Type: application/json' \
  -d '{"title":"Intro"}' | jq -r .id)

L=$(curl -s -X POST $H/api/modules/$M/lessons -H 'Content-Type: application/json' \
  -d '{"title":"What is ORM"}' | jq -r .id)

A=$(curl -s -X POST $H/api/lessons/$L/assignments -H 'Content-Type: application/json' \
  -d '{"title":"Homework","maxScore":100}' | jq -r .id)

curl -s -X POST "$H/api/courses/$C/enrollments?studentId=$S"
curl -s -X POST "$H/api/assignments/$A/submissions?studentId=$S" \
  -H 'Content-Type: application/json' -d '{"content":"my answer"}'
curl -s $H/api/courses/$C/structure | jq
```

## 10. Примеры ошибок

404:

```json
{"type":"about:blank","title":"Not Found","status":404,
 "detail":"Course 999 not found","code":"NOT_FOUND","timestamp":"2026-11-13T12:00:00Z"}
```

400 при валидации:

```json
{"type":"about:blank","title":"Bad Request","status":400,
 "detail":"Validation failed","code":"VALIDATION_ERROR",
 "errors":[{"field":"email","message":"must be a well-formed email address"}],
 "timestamp":"2026-11-13T12:00:00Z"}
```

409 при конфликте:

```json
{"type":"about:blank","title":"Conflict","status":409,
 "detail":"Student 5 has already submitted assignment 3","code":"CONFLICT"}
```

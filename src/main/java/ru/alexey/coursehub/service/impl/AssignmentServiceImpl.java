package ru.alexey.coursehub.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.alexey.coursehub.dto.request.CreateAssignmentRequest;
import ru.alexey.coursehub.dto.request.UpdateAssignmentRequest;
import ru.alexey.coursehub.entity.Assignment;
import ru.alexey.coursehub.entity.Lesson;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.AssignmentRepository;
import ru.alexey.coursehub.repository.LessonRepository;
import ru.alexey.coursehub.repository.SubmissionRepository;
import ru.alexey.coursehub.service.AssignmentService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentServiceImpl implements AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final LessonRepository lessonRepository;
    private final SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public Assignment create(Long lessonId, CreateAssignmentRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> NotFoundException.of("Lesson", lessonId));
        Assignment assignment = new Assignment();
        assignment.setLesson(lesson);
        assignment.setTitle(request.title().trim());
        assignment.setDescription(request.description());
        assignment.setDueDate(request.dueDate());
        assignment.setMaxScore(request.maxScore());
        Assignment saved = assignmentRepository.save(assignment);
        log.info("Created assignment {} for lesson {}", saved.getId(), lessonId);
        return saved;
    }

    @Override
    @Transactional
    public Assignment update(Long id, UpdateAssignmentRequest request) {
        Assignment assignment = get(id);
        if (StringUtils.hasText(request.title())) {
            assignment.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            assignment.setDescription(request.description());
        }
        if (request.dueDate() != null) {
            assignment.setDueDate(request.dueDate());
        }
        if (request.maxScore() != null) {
            assignment.setMaxScore(request.maxScore());
        }
        log.info("Updated assignment {}", id);
        return assignment;
    }

    @Override
    @Transactional(readOnly = true)
    public Assignment get(Long id) {
        return assignmentRepository.findByIdWithLesson(id).orElseThrow(() -> NotFoundException.of("Assignment", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Assignment> listByLesson(Long lessonId) {
        return assignmentRepository.findByLessonIdOrderByDueDateAsc(lessonId);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Assignment assignment = get(id);
        if (submissionRepository.countByAssignmentId(id) > 0) {
            throw new ConflictException("Assignment " + id + " has submissions and cannot be deleted");
        }
        assignmentRepository.delete(assignment);
        log.info("Deleted assignment {}", id);
    }
}

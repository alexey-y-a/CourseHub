package ru.alexey.coursehub.service;

import java.util.List;
import ru.alexey.coursehub.dto.request.CreateAssignmentRequest;
import ru.alexey.coursehub.dto.request.UpdateAssignmentRequest;
import ru.alexey.coursehub.entity.Assignment;

public interface AssignmentService {

    Assignment create(Long lessonId, CreateAssignmentRequest request);

    Assignment update(Long id, UpdateAssignmentRequest request);

    Assignment get(Long id);

    List<Assignment> listByLesson(Long lessonId);

    void delete(Long id);
}

package ru.alexey.coursehub.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.alexey.coursehub.dto.request.UpdateEnrollmentRequest;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;

public interface EnrollmentService {

    Enrollment enroll(Long courseId, Long studentId);

    Enrollment get(Long id);

    Enrollment updateStatus(Long id, UpdateEnrollmentRequest request);

    void cancel(Long id);

    void cancel(Long courseId, Long studentId);

    List<Enrollment> listByCourse(Long courseId);

    List<Enrollment> listByStudent(Long studentId);

    Page<Enrollment> pageByStudent(Long studentId, EnrollmentStatus status, Pageable pageable);

    long countByCourse(Long courseId);
}

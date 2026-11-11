package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Enrollment;
import ru.alexey.coursehub.entity.enums.EnrollmentStatus;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);

    boolean existsByCourseId(Long courseId);

    boolean existsByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    Page<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status, Pageable pageable);

    long countByCourseId(Long courseId);

    long countByStudentId(Long studentId);

    @Query("select e from Enrollment e join fetch e.student s where e.course.id = :courseId")
    List<Enrollment> findByCourseIdWithStudents(@Param("courseId") Long courseId);

    @Query("""
            select e from Enrollment e
            join fetch e.course c
            join fetch c.category
            join fetch c.teacher
            where e.student.id = :studentId
            """)
    List<Enrollment> findByStudentIdWithCourse(@Param("studentId") Long studentId);
}

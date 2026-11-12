package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    long countByCourseId(Long courseId);

    long countByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"course", "course.category", "course.teacher", "student"})
    Page<Enrollment> findByStudentId(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"course", "course.category", "course.teacher", "student"})
    Page<Enrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status, Pageable pageable);

    @Query("""
            select e from Enrollment e
            join fetch e.student
            join fetch e.course c
            where c.id = :courseId
            """)
    List<Enrollment> findByCourseIdWithStudents(@Param("courseId") Long courseId);

    @Query("""
            select e from Enrollment e
            join fetch e.course c
            join fetch c.category
            join fetch c.teacher
            join fetch e.student
            where e.student.id = :studentId
            """)
    List<Enrollment> findByStudentIdWithCourse(@Param("studentId") Long studentId);
}

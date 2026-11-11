package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Submission;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);

    boolean existsByStudentId(Long studentId);

    @EntityGraph(attributePaths = {"student"})
    List<Submission> findByAssignmentId(Long assignmentId);

    @EntityGraph(attributePaths = {"assignment"})
    Page<Submission> findByStudentId(Long studentId, Pageable pageable);

    long countByAssignmentId(Long assignmentId);

    @Query("select avg(s.score) from Submission s where s.assignment.id = :assignmentId and s.score is not null")
    Double findAverageScore(@Param("assignmentId") Long assignmentId);

    @Query("""
            select s from Submission s
            join fetch s.assignment a
            join fetch a.lesson l
            where s.student.id = :studentId and l.module.course.id = :courseId
            """)
    List<Submission> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}

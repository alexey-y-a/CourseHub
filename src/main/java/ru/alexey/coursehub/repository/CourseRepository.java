package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"category", "teacher"})
    Page<Course> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "teacher"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findByIdWithDetails(@Param("id") Long id);

    Page<Course> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Course> findByTeacherId(Long teacherId, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Course> findByTagsNameIgnoreCase(String tagName);

    boolean existsByCategoryId(Long categoryId);

    long countByTeacherId(Long teacherId);

    @Query("select c from Course c join fetch c.modules where c.id = :id")
    Optional<Course> findByIdWithModules(@Param("id") Long id);

    @Query("""
            select distinct c from Course c
            join c.modules m
            where m.id = :moduleId
            """)
    Optional<Course> findByModuleId(@Param("moduleId") Long moduleId);
}

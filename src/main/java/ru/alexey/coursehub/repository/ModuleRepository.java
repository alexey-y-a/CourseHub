package ru.alexey.coursehub.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.Module;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    List<Module> findByCourseIdOrderByOrderIndexAsc(Long courseId);

    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);

    long countByCourseId(Long courseId);

    @Query("select m from Module m join fetch m.lessons where m.id = :id")
    Optional<Module> findByIdWithLessons(@Param("id") Long id);

    @Query("select coalesce(max(m.orderIndex), 0) from Module m where m.course.id = :courseId")
    int findMaxOrderIndex(@Param("courseId") Long courseId);
}

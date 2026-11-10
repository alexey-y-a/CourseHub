package ru.alexey.coursehub.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.alexey.coursehub.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Tag> findAllByOrderByNameAsc();

    List<Tag> findAllByNameIn(Collection<String> names);

    List<Tag> findByCoursesId(Long courseId);
}

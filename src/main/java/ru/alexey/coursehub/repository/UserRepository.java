package ru.alexey.coursehub.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.alexey.coursehub.entity.User;
import ru.alexey.coursehub.entity.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    @Query("select u from User u where lower(u.name) like lower(concat('%', :text, '%'))")
    Page<User> searchByName(@Param("text") String text, Pageable pageable);

    @EntityGraph(attributePaths = {"profile"})
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdWithProfile(@Param("id") Long id);

    long countByRole(Role role);
}

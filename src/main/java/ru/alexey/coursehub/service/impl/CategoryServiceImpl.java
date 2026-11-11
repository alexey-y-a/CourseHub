package ru.alexey.coursehub.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alexey.coursehub.dto.request.CategoryRequest;
import ru.alexey.coursehub.entity.Category;
import ru.alexey.coursehub.exception.ConflictException;
import ru.alexey.coursehub.exception.NotFoundException;
import ru.alexey.coursehub.repository.CategoryRepository;
import ru.alexey.coursehub.repository.CourseRepository;
import ru.alexey.coursehub.service.CategoryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional
    public Category create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("Category " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        Category saved = categoryRepository.save(category);
        log.info("Created category {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryRequest request) {
        Category category = get(id);
        String name = request.name().trim();
        categoryRepository
                .findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Category " + name + " already exists");
                });
        category.setName(name);
        log.info("Updated category {}", id);
        return category;
    }

    @Override
    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> NotFoundException.of("Category", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> list() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = get(id);
        if (courseRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category " + id + " has courses and cannot be deleted");
        }
        categoryRepository.delete(category);
        log.info("Deleted category {}", id);
    }
}

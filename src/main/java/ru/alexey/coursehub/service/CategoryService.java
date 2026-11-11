package ru.alexey.coursehub.service;

import java.util.List;
import ru.alexey.coursehub.dto.request.CategoryRequest;
import ru.alexey.coursehub.entity.Category;

public interface CategoryService {

    Category create(CategoryRequest request);

    Category update(Long id, CategoryRequest request);

    Category get(Long id);

    List<Category> list();

    void delete(Long id);
}

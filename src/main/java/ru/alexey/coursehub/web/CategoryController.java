package ru.alexey.coursehub.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.alexey.coursehub.dto.request.CategoryRequest;
import ru.alexey.coursehub.dto.response.CategoryResponse;
import ru.alexey.coursehub.mapper.CatalogMapper;
import ru.alexey.coursehub.service.CategoryService;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category")
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return CatalogMapper.toResponse(categoryService.create(request));
    }

    @GetMapping
    @Operation(summary = "List categories")
    public List<CategoryResponse> list() {
        return categoryService.list().stream().map(CatalogMapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category")
    public CategoryResponse get(@PathVariable Long id) {
        return CatalogMapper.toResponse(categoryService.get(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rename category")
    public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return CatalogMapper.toResponse(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}

package com.project.demo.rest.category;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoryPage.getTotalPages());
        meta.setTotalElements(categoryPage.getTotalElements());
        meta.setPageNumber(categoryPage.getNumber() + 1);
        meta.setPageSize(categoryPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categories retreived successfully",
                categoryPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> addCategory(@RequestBody Category category, HttpServletRequest request) {
        categoryRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully",
                category, HttpStatus.OK, request);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody Category category, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if (foundCategory.isPresent()) {
            Category updatedCategory = foundCategory.get();
            updatedCategory.setName(category.getName());
            updatedCategory.setDescription(category.getDescription());
            categoryRepository.save(updatedCategory);
            return new GlobalResponseHandler().handleResponse("Category updated successfully",
                    category, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId, HttpServletRequest request) {
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        if(foundCategory.isPresent()) {
            categoryRepository.deleteById(categoryId);
            return new GlobalResponseHandler().handleResponse("Category deleted successfully",
                    foundCategory.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }
}

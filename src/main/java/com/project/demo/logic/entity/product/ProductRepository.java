package com.project.demo.logic.entity.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> getProductsByCategoryId(Long categoryId, Pageable pageable);
}

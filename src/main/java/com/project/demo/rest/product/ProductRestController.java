package com.project.demo.rest.product;

import com.project.demo.logic.entity.category.Category;
import com.project.demo.logic.entity.category.CategoryRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.http.Meta;
import com.project.demo.logic.entity.product.Product;
import com.project.demo.logic.entity.product.ProductRepository;
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
@RequestMapping("/products")
public class ProductRestController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Product> productPage = productRepository.findAll(pageable);
        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productPage.getTotalPages());
        meta.setTotalElements(productPage.getTotalElements());
        meta.setPageNumber(productPage.getNumber() + 1);
        meta.setPageSize(productPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retreived successfully",
                productPage.getContent(), HttpStatus.OK, meta);
    }

    @GetMapping("/category/{categoryId}/products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllByCategory(@PathVariable Long categoryId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {

            Pageable pageable = PageRequest.of(page-1, size);
            Page<Product> productsPage = productRepository.getProductsByCategoryId(categoryId, pageable);
            Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
            meta.setTotalPages(productsPage.getTotalPages());
            meta.setTotalElements(productsPage.getTotalElements());
            meta.setPageNumber(productsPage.getNumber() + 1);
            meta.setPageSize(productsPage.getSize());

            return new GlobalResponseHandler().handleResponse("Products retrieved successfully by category",
                    productsPage.getContent(), HttpStatus.OK, meta);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping("/category/{categoryId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProductToCategory(@PathVariable Long categoryId, @RequestBody Product product, HttpServletRequest request) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            product.setCategory(category.get());
            Product saveProduct = productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product created successfully",
                    saveProduct, HttpStatus.CREATED, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @RequestBody Product product, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);
        if(foundProduct.isPresent()) {
            product.setId(foundProduct.get().getId());
            product.setCategory(foundProduct.get().getCategory());
            productRepository.save(product);
            return new GlobalResponseHandler().handleResponse("Product updated successfully",
                    product, HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product id " + productId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, HttpServletRequest request) {
        Optional<Product> foundProduct = productRepository.findById(productId);
        if(foundProduct.isPresent()) {
            Optional<Category> category = categoryRepository.findById(foundProduct.get().getCategory().getId());
            category.get().getProducts().remove(foundProduct.get());
            productRepository.deleteById(foundProduct.get().getId());
            return new GlobalResponseHandler().handleResponse("Product deleted successfully",
                    foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product id " + productId + " not found"  ,
                    HttpStatus.NOT_FOUND, request);
        }
    }
}

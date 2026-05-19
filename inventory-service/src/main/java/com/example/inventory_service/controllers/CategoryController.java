package com.example.inventory_service.controllers;

import com.example.inventory_service.dtos.CategoryCreateRequestDTO;
import com.example.inventory_service.dtos.CategoryCreateResponseDTO;
import com.example.inventory_service.services.CategoryService;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@PreAuthorize("hasRole('ADMIN')")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryCreateResponseDTO> createCategory(@Valid  @RequestBody CategoryCreateRequestDTO categoryCreateRequestDTO){
        return new ResponseEntity<>(categoryService.createCategory(categoryCreateRequestDTO), HttpStatus.CREATED);
    }

}

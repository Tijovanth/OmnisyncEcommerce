package com.example.inventory_service.services;

import com.example.inventory_service.dtos.CategoryCreateRequestDTO;
import com.example.inventory_service.dtos.CategoryCreateResponseDTO;
import com.example.inventory_service.mappers.CategoryMapper;
import com.example.inventory_service.models.Category;
import com.example.inventory_service.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryCreateResponseDTO createCategory(CategoryCreateRequestDTO categoryCreateRequestDTO){
        Category category = categoryMapper.toEntity(categoryCreateRequestDTO);
        return categoryMapper.toDTO(categoryRepository.save(category));
    }
}

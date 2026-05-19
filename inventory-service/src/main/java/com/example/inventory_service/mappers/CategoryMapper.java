package com.example.inventory_service.mappers;

import com.example.inventory_service.dtos.CategoryCreateRequestDTO;
import com.example.inventory_service.dtos.CategoryCreateResponseDTO;
import com.example.inventory_service.models.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    Category toEntity(CategoryCreateRequestDTO categoryCreateRequestDTO);

    CategoryCreateResponseDTO toDTO(Category category);
}

package com.example.inventory_service.mappers;

import com.example.inventory_service.dtos.ProductCreateRequestDTO;
import com.example.inventory_service.dtos.ProductCreateResponseDTO;
import com.example.inventory_service.dtos.ProductGetResponseDTOs;
import com.example.inventory_service.models.Category;
import com.example.inventory_service.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductCreateRequestDTO productCreateRequestDTO);

    @Mapping(target = "stockQuantity", source = "stockQuantity")
    ProductCreateResponseDTO toDTO(Product Product);

    ProductGetResponseDTOs.ProductGetResponseDTO toGetDTO(Product product);

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "categoryName", source = "name")
    ProductCreateResponseDTO.ProductCategoryResponseDTO toCategoryDTO(Category category);
}

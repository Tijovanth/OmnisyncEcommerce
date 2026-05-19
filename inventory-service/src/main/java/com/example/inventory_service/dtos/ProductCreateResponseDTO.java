package com.example.inventory_service.dtos;

public record ProductCreateResponseDTO(

        Long id,

        String name,

        Long stockQuantity,

        Double price,

        ProductCategoryResponseDTO category
) {

    public record ProductCategoryResponseDTO(
            Long categoryId,
            String categoryName
    ){

    }

}

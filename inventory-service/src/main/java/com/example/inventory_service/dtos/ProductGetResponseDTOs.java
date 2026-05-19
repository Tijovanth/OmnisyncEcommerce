package com.example.inventory_service.dtos;

import java.util.List;

public record ProductGetResponseDTOs(

        List<ProductGetResponseDTO> products,
        int page,
        int perPage,
        boolean hasNext
) {

    public record ProductGetResponseDTO(

            Long id,

            String name,

            Long stockQuantity,

            Double price,

            ProductCreateResponseDTO.ProductCategoryResponseDTO category
    ){

    }
}


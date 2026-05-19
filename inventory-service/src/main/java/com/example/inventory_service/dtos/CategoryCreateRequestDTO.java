package com.example.inventory_service.dtos;

import jakarta.validation.constraints.NotBlank;

public record CategoryCreateRequestDTO(
        @NotBlank(message = "Name cannot be blank")
        String name) {
}

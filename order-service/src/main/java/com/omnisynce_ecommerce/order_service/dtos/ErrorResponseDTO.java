package com.omnisynce_ecommerce.order_service.dtos;

import java.time.LocalDateTime;

public record ErrorResponseDTO(

        LocalDateTime timestamp,
        String message,
        String path,
        int status,
        String error
) {
}

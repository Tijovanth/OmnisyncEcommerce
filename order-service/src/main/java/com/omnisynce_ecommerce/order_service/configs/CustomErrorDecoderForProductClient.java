package com.omnisynce_ecommerce.order_service.configs;

import com.omnisynce_ecommerce.order_service.exceptions.BadRequestException;
import com.omnisynce_ecommerce.order_service.exceptions.ResourceNotFoundException;
import com.omnisynce_ecommerce.order_service.exceptions.ServiceUnavailableException;
import feign.Response;
import feign.codec.ErrorDecoder;


public class CustomErrorDecoderForProductClient implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException("Product not found in Inventory Service");
            case 400 -> new BadRequestException("Invalid request sent to Inventory");
            case 503 -> new ServiceUnavailableException("Inventory Service is down");
            default -> new Exception("Generic Error " + response.reason());
        };
    }
}
package com.omnisynce_ecommerce.order_service.exceptions;

public class StockNotAvailableException extends RuntimeException {
    public StockNotAvailableException(String message) {
        super(message);
    }
}

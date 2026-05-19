package com.example.inventory_service.exceptions;

public class EventSerializationException extends RuntimeException {
    public EventSerializationException(String message) {
        super(message);
    }
}

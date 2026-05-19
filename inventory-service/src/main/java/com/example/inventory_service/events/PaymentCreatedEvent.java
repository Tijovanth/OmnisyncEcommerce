package com.example.inventory_service.events;

public record PaymentCreatedEvent(Long paymentId,
                                  Long orderId,
                                  Double amount) {
}

package com.example.inventory_service.events;

public record PaymentFailedEvent(Long orderId,
                                 Long paymentId,
                                 Double amount,
                                 String errorMessage) {
}

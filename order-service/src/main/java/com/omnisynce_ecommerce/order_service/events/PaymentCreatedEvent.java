package com.omnisynce_ecommerce.order_service.events;

public record PaymentCreatedEvent(Long paymentId,
                                  Long orderId,
                                  Double amount) {
}

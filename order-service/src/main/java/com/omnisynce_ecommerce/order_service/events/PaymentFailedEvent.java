package com.omnisynce_ecommerce.order_service.events;

public record PaymentFailedEvent(Long orderId,
                                 Long paymentId,
                                 Double amount,
                                 String errorMessage) {
}

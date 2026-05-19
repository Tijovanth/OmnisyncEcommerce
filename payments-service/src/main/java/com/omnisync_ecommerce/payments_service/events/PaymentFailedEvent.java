package com.omnisync_ecommerce.payments_service.events;

public record PaymentFailedEvent(
        Long orderId,
        Long paymentId,
        Double amount,
        String errorMessage
) implements OutBoxable{
    @Override
    public Long getAggregateId() {
        return this.paymentId;
    }

    @Override
    public String getAggregateType() {
        return "Payment";
    }

    @Override
    public String getTopic() {
        return "payment_topic";
    }

    @Override
    public String getEventType() {
        return "PaymentFailedEvent";
    }

    @Override
    public Long getCorrelationId() {
        return this.orderId;
    }
}

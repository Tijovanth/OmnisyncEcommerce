package com.omnisync_ecommerce.payments_service.events;


public record PaymentCreatedEvent(

        Long paymentId,
        Long orderId,
        Double amount
) implements OutBoxable {

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
        return "PaymentCreatedEvent";
    }

    @Override
    public Long getCorrelationId() {
        return this.orderId;
    }
}

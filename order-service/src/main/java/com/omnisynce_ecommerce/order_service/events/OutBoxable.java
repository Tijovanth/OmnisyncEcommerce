package com.omnisynce_ecommerce.order_service.events;

public interface OutBoxable {

    String getEventType();
    String getTopic();
    String getAggregateType();
    Long getAggregateId();
    Long getCorrelationId();
}

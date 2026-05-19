package com.omnisync_ecommerce.payments_service.events;

public interface OutBoxable {

    Long getAggregateId();

    String getAggregateType();

    String getTopic();

    String getEventType();

    Long getCorrelationId();
}

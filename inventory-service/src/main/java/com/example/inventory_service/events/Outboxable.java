package com.example.inventory_service.events;

public interface Outboxable {

    String getEventType();
    String getTopic();
    String getAggregateType();
    Long getAggregateId();
    Long getCorrelationId();
}

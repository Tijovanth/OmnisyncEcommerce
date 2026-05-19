package com.example.inventory_service.events;

import java.util.List;

public record InventoryReservedEvent(
        Long orderId,
        List<ReservedItems> reservedItemsList,
        Double totalAmount
) implements Outboxable{
    @Override
    public String getEventType() {
        return "InventoryReservedEvent";
    }

    @Override
    public String getTopic() {
        return "inventory_topic";
    }

    @Override
    public String getAggregateType() {
        return "Inventory";
    }

    @Override
    public Long getAggregateId() {
        return this.orderId;
    }

    @Override
    public Long getCorrelationId() {
        return this.orderId;
    }

    public record ReservedItems(Long productId, Long quantity){}
}

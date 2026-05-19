package com.omnisync_ecommerce.payments_service.events;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryReservedEvent(
        Long orderId,
        Double totalAmount
) {
}

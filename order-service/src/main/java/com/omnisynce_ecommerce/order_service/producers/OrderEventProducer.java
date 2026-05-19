package com.omnisynce_ecommerce.order_service.producers;

import com.omnisynce_ecommerce.order_service.events.OrderCreatedEvent;
import com.omnisynce_ecommerce.order_service.events.OutBoxable;

public interface OrderEventProducer {

    void publish(OutBoxable orderEvents);
}

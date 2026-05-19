package com.omnisync_ecommerce.payments_service.producers;

import com.omnisync_ecommerce.payments_service.events.OutBoxable;
import com.omnisync_ecommerce.payments_service.events.PaymentCreatedEvent;

public interface PaymentEventProducer {

    void publish(OutBoxable paymentEvent);
}

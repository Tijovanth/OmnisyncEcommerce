package com.omnisync_ecommerce.payments_service.services;

import com.omnisync_ecommerce.payments_service.events.InventoryReservedEvent;
import com.omnisync_ecommerce.payments_service.events.PaymentCreatedEvent;
import com.omnisync_ecommerce.payments_service.events.PaymentFailedEvent;
import com.omnisync_ecommerce.payments_service.models.Payment;
import com.omnisync_ecommerce.payments_service.models.PaymentStatus;
import com.omnisync_ecommerce.payments_service.producers.PaymentEventProducer;
import com.omnisync_ecommerce.payments_service.repositories.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentEventProducer paymentEventProducer;

    @Transactional
    public void createPayment(InventoryReservedEvent event){

        if(paymentRepository.existsByOrderId(event.orderId())){
            log.warn("Payment will not created because of the duplicate event");
            return;
        }

        Payment payment = new Payment();
        payment.setOrderId(event.orderId());
        payment.setAmount(event.totalAmount());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        try{
          //  throw new RuntimeException("Payment Gateway failed");
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            log.info("Payment successful for the order {}", event.orderId());
            PaymentCreatedEvent paymentCreatedEvent = new PaymentCreatedEvent(payment.getId(), event.orderId(), event.totalAmount());
            paymentEventProducer.publish(paymentCreatedEvent);
        }catch(Exception e){
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            log.error("Payment failed from the gateway side for the order {}", event.orderId());
            PaymentFailedEvent paymentFailedEvent = new PaymentFailedEvent(payment.getOrderId(), payment.getId(), payment.getAmount(), "Failed in payment gateway");
            paymentEventProducer.publish(paymentFailedEvent);
        }
    }
}

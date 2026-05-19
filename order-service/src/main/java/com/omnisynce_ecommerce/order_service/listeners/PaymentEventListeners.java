package com.omnisynce_ecommerce.order_service.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisynce_ecommerce.order_service.events.PaymentCreatedEvent;
import com.omnisynce_ecommerce.order_service.events.PaymentFailedEvent;
import com.omnisynce_ecommerce.order_service.model.OrderStatus;
import com.omnisynce_ecommerce.order_service.services.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class PaymentEventListeners {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderService orderService;

    @KafkaListener(topics = "payment_topic", groupId = "order_service_group")
    public void consumePaymentEvents(ConsumerRecord<String,String> record){
        try {
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String userId = getHeaderAsString(record, "X-User-Id");
            String userRoles = getHeaderAsString(record, "X-User-Roles");
            String traceId = getHeaderAsString(record, "X-B3-TraceId");
            MDC.put("traceId", Objects.requireNonNullElse(traceId, "unknown-async-trace"));

            reconstructSecurityContext(userId, userRoles);

            String rawJson = record.value();

            switch(eventType){
                case "PaymentCreatedEvent" -> {
                    PaymentCreatedEvent paymentCreatedEvent = objectMapper.readValue(rawJson, PaymentCreatedEvent.class);
                    orderService.updateOrderStatus(paymentCreatedEvent.orderId(), OrderStatus.COMPLETED);
                }
                case "PaymentFailedEvent" -> {
                    PaymentFailedEvent paymentFailedEvent = objectMapper.readValue(rawJson,PaymentFailedEvent.class);
                    orderService.updateOrderStatus(paymentFailedEvent.orderId(), OrderStatus.FAILED);
                }
                default ->
                        log.error("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka record: {}", record.key(), e);
        }finally {
            SecurityContextHolder.clearContext();
            MDC.clear();
        }
    }

    private void reconstructSecurityContext(String userId, String userRoles){
        if(userId == null){
            userId = "system-user";
        }

        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if(userRoles != null && !userRoles.isBlank()){
            authorities = Arrays.stream(userRoles.split(","))
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }

    private String getHeaderAsString(ConsumerRecord<String,String> record, String headerKey){
        Header header = record.headers().lastHeader(headerKey);
        if(header != null && header.value() != null){
            return new String(header.value());
        }
        return null;
    }
}

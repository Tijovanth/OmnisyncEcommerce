package com.example.inventory_service.event_listeners;

import com.example.inventory_service.events.PaymentCreatedEvent;
import com.example.inventory_service.events.PaymentFailedEvent;
import com.example.inventory_service.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

@Slf4j
@Component
public class PaymentEventListener {

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = "payment_topic", groupId = "inventory_service_group")
    public void consumePaymentEvents(ConsumerRecord<String, String> record) {
        try{
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String userId = getHeaderAsString(record, "X-User-Id");
            String userRoles = getHeaderAsString(record, "X-User-Roles");

            reconstructSecurityContext(userId, userRoles);

            String rawJson = record.value();

            switch (eventType){
                case "PaymentFailedEvent" -> {
                    PaymentFailedEvent event = objectMapper.readValue(rawJson, PaymentFailedEvent.class);
                    MDC.put("orderId", String.valueOf(event.orderId()));
                    productService.restoreStock(event);
                }
                case "PaymentCreatedEvent" -> {
                    PaymentCreatedEvent event = objectMapper.readValue(rawJson, PaymentCreatedEvent.class);
                    MDC.put("orderId", String.valueOf(event.orderId()));
                    productService.updateInventoryStatus(event);
                }
            }

        }catch(Exception e){
            log.error("Failed to process Kafka record: {}", record.key(), e);
        }finally{
            MDC.remove("orderId");
            SecurityContextHolder.clearContext();
        }
    }

    private void reconstructSecurityContext(String userId, String rolesString){
        if (userId == null || userId.isBlank()) {
            userId = "system-user";
        }
        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if (rolesString != null && !rolesString.isBlank()) {
            authorities = Arrays.stream(rolesString.split(","))
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getHeaderAsString(ConsumerRecord<String, String> record, String headerKey){
        Header header = record.headers().lastHeader(headerKey);
        if(header != null && header.value() != null){
            return new String(header.value());
        }
        return null;
    }

}

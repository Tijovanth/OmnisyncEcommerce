package com.omnisync_ecommerce.payments_service.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisync_ecommerce.payments_service.events.InventoryReservedEvent;
import com.omnisync_ecommerce.payments_service.services.PaymentService;
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
public class InventoryEventListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(topics = "inventory_topic", groupId = "payments_service_group")
    public void consumeInventoryEvents(ConsumerRecord<String,String> record){
        try{
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String userId = getHeaderAsString(record, "X-User-Id");
            String roles = getHeaderAsString(record, "X-User-Roles");

            reconstructSecurityContext(userId,roles);

            String rawJson = record.value();
            switch (eventType){
                case "InventoryReservedEvent" -> {
                    InventoryReservedEvent event = objectMapper.readValue(rawJson, InventoryReservedEvent.class);
                    MDC.put("orderId", String.valueOf(event.orderId()));
                    paymentService.createPayment(event);
                }
                default ->
                    log.error("Unknown event type: {}", eventType);
            }
        }catch(Exception e){
            log.error("Failed to process Kafka record: {}", record.key(), e);
        }finally{
            SecurityContextHolder.clearContext();
            MDC.remove("orderId");
        }
    }

    private void reconstructSecurityContext(String userId, String roles){
        if (userId == null || userId.isBlank()) {
            userId = "system-user";
        }

        List<SimpleGrantedAuthority> authorities = Collections.emptyList();
        if (roles != null && !roles.isBlank()) {
            authorities = Arrays.stream(roles.split(","))
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }

        UsernamePasswordAuthenticationToken user = new UsernamePasswordAuthenticationToken(userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(user);
    }

    private String getHeaderAsString(ConsumerRecord<String,String> record, String headerKey){
        Header header = record.headers().lastHeader(headerKey);
        if(header != null && header.value() != null){
            return new String(header.value());
        }
        return null;
    }

}

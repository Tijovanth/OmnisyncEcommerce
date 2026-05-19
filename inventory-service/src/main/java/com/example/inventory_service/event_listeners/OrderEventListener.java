package com.example.inventory_service.event_listeners;

import com.example.inventory_service.events.OrderCreatedEvent;
import com.example.inventory_service.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderEventListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductService productService;


    @KafkaListener(topics = "orders_topic", groupId = "inventory_service_group")
    public void consumeOrderEvents(ConsumerRecord<String, String> record){
        try{
            String eventType = new String(record.headers().lastHeader("eventType").value());
            String userId = getHeaderAsString(record, "X-User-Id");
            String userRoles = getHeaderAsString(record, "X-User-Roles");
            reconstructSecurityContext(userId, userRoles);
            String rawJsonPayload = record.value();
            switch (eventType){
                case "OrderCreatedEvent" ->{
                        OrderCreatedEvent orderCreatedEvent = objectMapper.readValue(rawJsonPayload, OrderCreatedEvent.class);
                        MDC.put("orderId", String.valueOf(orderCreatedEvent.orderId()));
                        productService.reduceStock(orderCreatedEvent);
                }
                default ->
                    log.error("Unknown event type: {}", eventType);
            }
        }catch(Exception e){
            log.error("Failed to process Kafka record: {}", record.key(), e);
        }finally {
            SecurityContextHolder.clearContext();
            MDC.remove("orderId");
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
        log.info("SuccessFully construct the security context object");
    }

    private String getHeaderAsString(ConsumerRecord<String, String> record, String headerKey){
        Header header = record.headers().lastHeader(headerKey);
        if(header != null && header.value() != null){
            return new String(header.value());
        }
        return null;
    }
}

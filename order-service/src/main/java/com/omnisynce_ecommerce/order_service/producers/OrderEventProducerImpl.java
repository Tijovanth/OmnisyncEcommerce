package com.omnisynce_ecommerce.order_service.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisynce_ecommerce.order_service.events.OrderCreatedEvent;
import com.omnisynce_ecommerce.order_service.events.OutBoxable;
import com.omnisynce_ecommerce.order_service.exceptions.EventSerializationException;
import com.omnisynce_ecommerce.order_service.model.OutBoxStatus;
import com.omnisynce_ecommerce.order_service.model.OutboxEvent;
import com.omnisynce_ecommerce.order_service.repositories.OutBoxEventRepository;
import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderEventProducerImpl implements  OrderEventProducer{

    private final OutBoxEventRepository outBoxEventRepository;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;

    public OrderEventProducerImpl(OutBoxEventRepository outBoxEventRepository, ObjectMapper objectMapper, OpenTelemetry openTelemetry) {
        this.outBoxEventRepository = outBoxEventRepository;
        this.objectMapper = objectMapper;
        this.openTelemetry = openTelemetry;
    }

    @Override
    public void publish(OutBoxable orderEvents) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = "userId";
            String roles = "";

            if(auth != null && auth.getPrincipal() instanceof  Jwt jwt){
                userId = jwt.getClaimAsString("sub");
                roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
            }

            Map<String,String> propagationHeaders = new HashMap<>();
            openTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .inject(io.opentelemetry.context.Context.current(),
                            propagationHeaders,
                            new io.opentelemetry.context.propagation.TextMapSetter<>() {
                                @Override
                                public void set(Map<String, String> carrier, String key, String value) {
                                    carrier.put(key, value);
                                }
                            });

            String traceContext = propagationHeaders.getOrDefault("traceparent", "no-trace");

            OutboxEvent outboxEvent = OutboxEvent.builder().aggregateType("ORDER")
                    .aggregateId(orderEvents.getAggregateId())
                    .eventType(orderEvents.getEventType())
                    .topic(orderEvents.getTopic())
                    .payload(objectMapper.writeValueAsString(orderEvents))
                    .status(OutBoxStatus.PENDING)
                    .retry(0)
                    .sub(userId)
                    .roles(roles)
                    .traceId(traceContext)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            outBoxEventRepository.save(outboxEvent);
            log.debug("Safely stored {} in outbox for Order {}", orderEvents.getClass().getSimpleName(), orderEvents.getAggregateId());
        }catch (JsonProcessingException e) {
            log.error("Failed to serialize Outbox Event for order: {}", orderEvents.getAggregateId(), e);
            throw new EventSerializationException("Unable to process order event");
        }
    }
}

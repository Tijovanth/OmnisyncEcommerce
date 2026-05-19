package com.example.inventory_service.producers;

import com.example.inventory_service.events.Outboxable;
import com.example.inventory_service.exceptions.EventSerializationException;
import com.example.inventory_service.models.OutBoxEvent;
import com.example.inventory_service.models.OutBoxStatus;
import com.example.inventory_service.repositories.OutBoxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class InventoryEventProducer {

    @Autowired
    private final OutBoxEventRepository outBoxEventRepository;

    @Autowired
    private final ObjectMapper objectMapper;

    @Autowired
    private final OpenTelemetry openTelemetry;

    public void publish(Outboxable inventoryEvents) {
        try{

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = "system";
            String roles = "";

            if(auth.getPrincipal() != null){
                userId = auth.getPrincipal().toString();
            }
            if(!auth.getAuthorities().isEmpty()){
                roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
            }

            Map<String, String> propagationHeaders = new HashMap<>();
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

            OutBoxEvent outBoxEvent = OutBoxEvent.builder()
                    .aggregateId(inventoryEvents.getAggregateId())
                    .eventType(inventoryEvents.getEventType())
                    .topic(inventoryEvents.getTopic())
                    .aggregateType(inventoryEvents.getAggregateType())
                    .payload(objectMapper.writeValueAsString(inventoryEvents))
                    .status(OutBoxStatus.PENDING)
                    .retry(0)
                    .nextAttemptAt(LocalDateTime.now())
                    .sub(userId)
                    .roles(roles)
                    .traceId(traceContext)
                    .build();
            outBoxEventRepository.save(outBoxEvent);
            log.debug("Safely stored {} in outbox for the order {}", inventoryEvents.getClass().getSimpleName(), inventoryEvents.getAggregateId());
        }catch(JsonProcessingException e){
            log.error("Failed to serialize Outbox Event for the order : {}", inventoryEvents.getAggregateId(), e);
            throw new EventSerializationException("Unable to process inventory event");
        }
    }
}

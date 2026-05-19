package com.omnisync_ecommerce.payments_service.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisync_ecommerce.payments_service.events.OutBoxable;
import com.omnisync_ecommerce.payments_service.events.PaymentCreatedEvent;
import com.omnisync_ecommerce.payments_service.exceptions.EventSerializationException;
import com.omnisync_ecommerce.payments_service.models.OutBoxEvent;
import com.omnisync_ecommerce.payments_service.models.OutBoxStatus;
import com.omnisync_ecommerce.payments_service.repositories.OutBoxEventRepository;
import io.opentelemetry.api.OpenTelemetry;
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
public class PaymentEventProducerImpl implements PaymentEventProducer{

    @Autowired
    private OutBoxEventRepository outBoxEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OpenTelemetry openTelemetry;

    @Override
    public void publish(OutBoxable paymentEvent) {
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


            OutBoxEvent o = OutBoxEvent.builder().aggregateType(paymentEvent.getAggregateType())
                    .aggregateId(paymentEvent.getAggregateId())
                    .payload(objectMapper.writeValueAsString(paymentEvent))
                    .topic(paymentEvent.getTopic())
                    .eventType(paymentEvent.getEventType())
                    .status(OutBoxStatus.PENDING)
                    .retry(0)
                   .sub(userId)
                   .roles(roles)
                   .traceId(traceContext)
                    .nextAttemptAt(LocalDateTime.now())
                    .build();
            outBoxEventRepository.save(o);
            log.info("Safely stored {} in outbox for Order {}", paymentEvent.getClass().getSimpleName(), paymentEvent.getAggregateId());
        }catch (JsonProcessingException e){
            log.error("Failed to serialize Outbox Event for this order {}", paymentEvent.getAggregateId(), e);
            throw new EventSerializationException("Unable to process order event");
        }
    }
}

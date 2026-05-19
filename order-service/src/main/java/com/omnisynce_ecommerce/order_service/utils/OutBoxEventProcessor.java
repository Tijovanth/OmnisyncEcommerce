package com.omnisynce_ecommerce.order_service.utils;

import com.omnisynce_ecommerce.order_service.model.OutBoxStatus;
import com.omnisynce_ecommerce.order_service.model.OutboxEvent;
import com.omnisynce_ecommerce.order_service.repositories.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OutBoxEventProcessor {

    private final OutBoxEventRepository outBoxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(OutboxEvent event){
        try {
            List<Header> headers = new ArrayList<>();
            headers.add(new RecordHeader("eventType", event.getEventType().getBytes()));
            if (event.getSub() != null) {
                headers.add(new RecordHeader("X-User-Id", event.getSub().getBytes()));
            }
            if (event.getRoles() != null) {
                headers.add(new RecordHeader("X-User-Roles", event.getRoles().getBytes()));
            }
            if (event.getTraceId() != null && event.getTraceId().startsWith("00-")) {
                headers.add(new RecordHeader("traceparent", event.getTraceId().getBytes(StandardCharsets.UTF_8)));
                String extractedTraceId = event.getTraceId().split("-")[1];
                MDC.put("traceId", extractedTraceId);
            }
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    event.getTopic(),
                    null,
                    null,
                    event.getAggregateId().toString(),
                    event.getPayload(),
                    headers
            );
            try (Producer<String, String> rawProducer = kafkaTemplate.getProducerFactory().createProducer()) {
                rawProducer.send(record).get(12, TimeUnit.SECONDS);
            }
            event.setStatus(OutBoxStatus.PROCESSED);
            log.info("Processed the event for aggregateId {}", event.getAggregateId());
            outBoxEventRepository.save(event);
        } catch (Exception e) {
            int maxRetries = 5;
            if (event.getRetry() >= maxRetries) {
                log.error("Failed to process outbox event for aggregateId: {} and retry is: {}", event.getAggregateId(), event.getRetry());
                event.setStatus(OutBoxStatus.FAILED);
            } else {
                event.setRetry(event.getRetry() + 1);
                long baseDelaySeconds = 5L * (long) Math.pow(2, event.getRetry() + 1);
                double jitterMultiplier = 0.8 + (Math.random() * 0.4);
                long finalDelaySeconds = (long) (baseDelaySeconds * jitterMultiplier);
                event.setNextAttemptAt(LocalDateTime.now().plusSeconds(finalDelaySeconds));
                log.error("Retry this event with aggregateId: {}", event.getAggregateId());
                log.error("Kafka failed", e);
            }
            outBoxEventRepository.save(event);
        }finally {
            MDC.remove("traceId");
        }

    }

}


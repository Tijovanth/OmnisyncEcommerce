package com.omnisync_ecommerce.payments_service.utils;

import com.omnisync_ecommerce.payments_service.models.OutBoxEvent;
import com.omnisync_ecommerce.payments_service.models.OutBoxStatus;
import com.omnisync_ecommerce.payments_service.repositories.OutBoxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class OutBoxEventProcessor {

    private final KafkaTemplate<String,String> kafkaTemplate;
    private final OutBoxEventRepository outBoxEventRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleEvent(OutBoxEvent outBoxEvent){
        try{
            List<Header> headers = new ArrayList<>();
            headers.add(new RecordHeader("eventType", outBoxEvent.getEventType().getBytes()));
            if(outBoxEvent.getSub() != null){
                headers.add(new RecordHeader("X-User-Id",outBoxEvent.getSub().getBytes()));
            }

            if(outBoxEvent.getRoles() != null){
                headers.add(new RecordHeader("X-User-Roles",outBoxEvent.getRoles().getBytes()));
            }

            if (outBoxEvent.getTraceId() != null && outBoxEvent.getTraceId().startsWith("00-")) {
                headers.add(new RecordHeader("traceparent", outBoxEvent.getTraceId().getBytes(StandardCharsets.UTF_8)));
                String extractedTraceId = outBoxEvent.getTraceId().split("-")[1];
                MDC.put("traceId", extractedTraceId);
            }
            ProducerRecord<String,String> record = new ProducerRecord<>(
                    outBoxEvent.getTopic(),
                    null,
                    null,
                    outBoxEvent.getAggregateId().toString(),
                    outBoxEvent.getPayload(),
                    headers
            );
            kafkaTemplate.send(record).get(12, TimeUnit.SECONDS);
            outBoxEvent.setStatus(OutBoxStatus.PROCESSED);
            log.info("Processed the event for aggregateId {}", outBoxEvent.getAggregateId());
            outBoxEventRepository.save(outBoxEvent);
        }catch(Exception e){
            int maxRetries = 5;
            if(maxRetries < outBoxEvent.getRetry()){
                log.error("Failed to process outbox event for aggregateId: {} and retry is: {}", outBoxEvent.getAggregateId(), outBoxEvent.getRetry());
                outBoxEvent.setStatus(OutBoxStatus.FAILED);
            }else{
                outBoxEvent.setRetry(outBoxEvent.getRetry() + 1);
                long baseDelaySeconds = 5L * (long) Math.pow(2, outBoxEvent.getRetry() + 1);
                double jitterMultiplier = 0.8 + (Math.random() * 0.4);
                long finalDelaySeconds = (long) (baseDelaySeconds * jitterMultiplier);
                outBoxEvent.setNextAttemptAt(LocalDateTime.now().plusSeconds(finalDelaySeconds));
                log.error("Retry this event with aggregateId: {}", outBoxEvent.getAggregateId());
            }
            outBoxEventRepository.save(outBoxEvent);
        }finally {
            MDC.remove("traceId");
        }
    }
}

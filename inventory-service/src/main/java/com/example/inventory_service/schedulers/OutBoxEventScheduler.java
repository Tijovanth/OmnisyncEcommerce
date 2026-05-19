package com.example.inventory_service.schedulers;

import com.example.inventory_service.models.OutBoxEvent;
import com.example.inventory_service.models.OutBoxStatus;
import com.example.inventory_service.repositories.OutBoxEventRepository;
import com.example.inventory_service.utils.OutBoxEventProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutBoxEventScheduler {

    private final OutBoxEventRepository outBoxEventRepository;
    private final OutBoxEventProcessor outBoxEventProcessor;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(name = "outbox_relay_task", lockAtMostFor = "10s", lockAtLeastFor = "2s")
    public void processOutBoxEvents() {
        log.info("Scheduler running... looking for events.");
        List<OutBoxEvent> outboxEvents = outBoxEventRepository.findTop100ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(OutBoxStatus.PENDING, LocalDateTime.now());
        log.info("Found {} events to process.", outboxEvents.size());
        for(OutBoxEvent event : outboxEvents){
            try{
                log.info("Processing event ID: {}", event.getId());
                outBoxEventProcessor.processSingleEvent(event);
                log.info("Successfully processed event ID: {}", event.getId());
            }catch(Exception e){
                log.error("FAILED to process event ID: {}. Error: {}", event.getId(), e.getMessage(), e);
            }
        }
    }
}

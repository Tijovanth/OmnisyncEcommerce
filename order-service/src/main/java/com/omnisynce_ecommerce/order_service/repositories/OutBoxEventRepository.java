package com.omnisynce_ecommerce.order_service.repositories;

import com.omnisynce_ecommerce.order_service.model.OutBoxStatus;
import com.omnisynce_ecommerce.order_service.model.OutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutBoxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
            OutBoxStatus status,
            LocalDateTime localDateTime
    );
}

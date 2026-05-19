package com.example.inventory_service.repositories;

import com.example.inventory_service.models.OutBoxEvent;
import com.example.inventory_service.models.OutBoxStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface  OutBoxEventRepository extends JpaRepository<OutBoxEvent,Long> {

    List<OutBoxEvent> findTop100ByStatusAndNextAttemptAtBeforeOrderByNextAttemptAtAsc(
            OutBoxStatus status,
            LocalDateTime localDateTime);
}

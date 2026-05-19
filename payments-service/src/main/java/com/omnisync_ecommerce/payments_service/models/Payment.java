package com.omnisync_ecommerce.payments_service.models;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(columnList = "order_id")
})
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pay_seq")
    @SequenceGenerator(name = "pay_seq")
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @CreatedDate
    private LocalDateTime createdAt;
}

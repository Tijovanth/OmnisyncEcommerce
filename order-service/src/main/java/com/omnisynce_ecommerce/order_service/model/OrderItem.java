package com.omnisynce_ecommerce.order_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_items_seq")
    @SequenceGenerator(name = "order_items_seq")
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_fk_id", referencedColumnName = "id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    Double price;

    private Long quantity;
}

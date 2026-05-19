package com.example.inventory_service.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_name", columnList = "name"),
        @Index(name = "idx_product_price", columnList = "price")
})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class Product extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pro_seq")
    @SequenceGenerator(name = "pro_seq")
    Long id;

    String name;

    Long stockQuantity;

    Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_fk_id", referencedColumnName = "id")
    Category category;
}

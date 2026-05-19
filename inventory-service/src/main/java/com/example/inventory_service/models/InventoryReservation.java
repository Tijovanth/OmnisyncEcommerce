package com.example.inventory_service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "inventory_reservations", indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
})
public class InventoryReservation extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ReservedItems> reservedItemList = new ArrayList<>();

    public void addReservedItem(ReservedItems reservedItems){
        reservedItemList.add(reservedItems);
        reservedItems.setReservation(this);
    }

    public void removeReservedItem(ReservedItems reservedItems){
        reservedItemList.remove(reservedItems);
        reservedItems.setReservation(null);
    }
}

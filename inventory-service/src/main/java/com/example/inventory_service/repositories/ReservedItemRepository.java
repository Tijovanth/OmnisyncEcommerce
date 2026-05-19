package com.example.inventory_service.repositories;

import com.example.inventory_service.models.ReservedItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservedItemRepository extends JpaRepository<ReservedItems, Long> {
}

package com.example.inventory_service.repositories;

import com.example.inventory_service.models.Product;
import jakarta.persistence.LockModeType;
import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("select p from Product p join fetch p.category c")
    Slice<Product> findProducts(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p join fetch p.category c where p.id = :productId")
    Product findByIdForUpdate(@Param("productId") Long productId);

    @Modifying
    @Query("update Product p SET p.stockQuantity = :currentStockQuantity where p.id = :productId ")
    void updateStockQuantity(@Param("productId") Long productId, @Param("currentStockQuantity") Long currentStockQuantity);

}

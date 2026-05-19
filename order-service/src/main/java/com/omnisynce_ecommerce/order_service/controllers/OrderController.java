package com.omnisynce_ecommerce.order_service.controllers;

import com.omnisynce_ecommerce.order_service.dtos.OrderCreateRequestDTO;
import com.omnisynce_ecommerce.order_service.dtos.OrderCreateResponseDTO;
import com.omnisynce_ecommerce.order_service.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderCreateResponseDTO createOrder(@Valid @RequestBody OrderCreateRequestDTO orderCreateRequestDTO) {
        return orderService.createOrder(orderCreateRequestDTO);
    }
}

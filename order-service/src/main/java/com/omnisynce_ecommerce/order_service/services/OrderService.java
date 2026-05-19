package com.omnisynce_ecommerce.order_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnisynce_ecommerce.order_service.dtos.InventoryProductDTO;
import com.omnisynce_ecommerce.order_service.dtos.OrderCreateRequestDTO;
import com.omnisynce_ecommerce.order_service.dtos.OrderCreateResponseDTO;
import com.omnisynce_ecommerce.order_service.events.OrderCreatedEvent;
import com.omnisynce_ecommerce.order_service.events.PaymentCreatedEvent;
import com.omnisynce_ecommerce.order_service.exceptions.EventSerializationException;
import com.omnisynce_ecommerce.order_service.exceptions.ServiceUnavailableException;
import com.omnisynce_ecommerce.order_service.exceptions.StockNotAvailableException;
import com.omnisynce_ecommerce.order_service.feignClients.ProductFeignClient;
import com.omnisynce_ecommerce.order_service.model.*;
import com.omnisynce_ecommerce.order_service.producers.OrderEventProducer;
import com.omnisynce_ecommerce.order_service.repositories.OrderRepository;
import com.omnisynce_ecommerce.order_service.repositories.OutBoxEventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductFeignClient productFeignClient;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository,ProductFeignClient productFeignClient,  OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.productFeignClient = productFeignClient;
        this.orderEventProducer = orderEventProducer;
    }

    @CircuitBreaker(name = "product-service", fallbackMethod = "fallBackMethodOfCreateOrder")
    @Transactional
    public OrderCreateResponseDTO createOrder(OrderCreateRequestDTO orderCreateRequestDTO){
        log.info("Executing placeOrder endpoint - checking Trace ID generation");
        double totalAmount = 0.0;

        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderStatus(OrderStatus.PENDING);

        List<OrderCreatedEvent.OrderItemDto> orderItemDtoList = new ArrayList<>();

        for(OrderCreateRequestDTO.OrderItemCreateRequestDTO orderItemCreateRequestDTO : orderCreateRequestDTO.orderItems()){
            InventoryProductDTO inventoryProductDTO = productFeignClient.getProductById(orderItemCreateRequestDTO.productId());

            if(inventoryProductDTO.stockQuantity() < orderItemCreateRequestDTO.quantity()){
                throw new StockNotAvailableException("Stock is not available for the select");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(orderItemCreateRequestDTO.quantity());
            orderItem.setProductId(orderItemCreateRequestDTO.productId());
            orderItem.setPrice(inventoryProductDTO.price());
            order.addOrderItem(orderItem);

            OrderCreatedEvent.OrderItemDto orderItemDto = new OrderCreatedEvent.OrderItemDto(orderItem.getProductId(),orderItem.getQuantity(),orderItem.getPrice());
            orderItemDtoList.add(orderItemDto);

            totalAmount += inventoryProductDTO.price() * orderItemCreateRequestDTO.quantity();
            //productFeignClient.reduceStock(orderItemCreateRequestDTO.productId(), inventoryProductDTO.stockQuantity() - orderItemCreateRequestDTO.quantity());
        }
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(order.getId(), order.getOrderNumber(), totalAmount, orderItemDtoList);
        orderEventProducer.publish(orderCreatedEvent);
        return new OrderCreateResponseDTO("Order Created Successfully", order.getOrderNumber());
    }

    public OrderCreateResponseDTO fallBackMethodOfCreateOrder(OrderCreateRequestDTO orderCreateRequestDTO, Throwable t){
        log.error("Inventory service is down");
        throw new ServiceUnavailableException("Service is unavailable");
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status){
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("No Order Found"));
        if (order.getOrderStatus() != status){
            order.setOrderStatus(status);
            orderRepository.save(order);
            log.info("Order Completed Successfully");
        }else{
            log.warn("Order is already processed");
        }
    }
}

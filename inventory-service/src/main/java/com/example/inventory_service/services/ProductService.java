package com.example.inventory_service.services;

import com.example.inventory_service.dtos.ProductCreateRequestDTO;
import com.example.inventory_service.dtos.ProductCreateResponseDTO;
import com.example.inventory_service.dtos.ProductGetResponseDTOs;
import com.example.inventory_service.events.InventoryReservedEvent;
import com.example.inventory_service.events.OrderCreatedEvent;
import com.example.inventory_service.events.PaymentCreatedEvent;
import com.example.inventory_service.events.PaymentFailedEvent;
import com.example.inventory_service.exceptions.StockNotAvailableException;
import com.example.inventory_service.mappers.ProductMapper;
import com.example.inventory_service.models.InventoryReservation;
import com.example.inventory_service.models.Product;
import com.example.inventory_service.models.ReservationStatus;
import com.example.inventory_service.models.ReservedItems;
import com.example.inventory_service.producers.InventoryEventProducer;
import com.example.inventory_service.repositories.CategoryRepository;
import com.example.inventory_service.repositories.InventoryReservationRepository;
import com.example.inventory_service.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final InventoryEventProducer inventoryEventProducer;
    private final InventoryReservationRepository inventoryReservationRepository;


    public ProductService(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository, InventoryEventProducer inventoryEventProducer, InventoryReservationRepository inventoryReservationRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
        this.inventoryEventProducer = inventoryEventProducer;
        this.inventoryReservationRepository = inventoryReservationRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ProductCreateResponseDTO createProduct(ProductCreateRequestDTO requestDTO){
        Product product = productMapper.toEntity(requestDTO);
        product.setCategory(categoryRepository.getReferenceById(requestDTO.categoryId()));
        Product savedProduct = productRepository.save(product);
        return productMapper.toDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductGetResponseDTOs.ProductGetResponseDTO getProduct(Long id){
        log.info("Executing getProduct endpoint - checking Trace ID generation");
        Product product = productRepository.findById(id).orElseThrow(RuntimeException :: new);
        return productMapper.toGetDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductGetResponseDTOs getProducts(int page, int perPage, String sortBy, Sort.Direction direction){
        Pageable pageable = PageRequest.of(page,perPage, Sort.by(direction, sortBy));
        Slice<Product> products = productRepository.findProducts(pageable);
        List<ProductGetResponseDTOs.ProductGetResponseDTO> productList = products.getContent().stream().map(productMapper::toGetDTO).toList();
        return new ProductGetResponseDTOs(productList,products.getNumber(),products.getSize(),products.hasNext());
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    @Transactional
    public void reduceStock(Long id, Long quantity) {
        Product product = productRepository.findByIdForUpdate(id);
        if(product.getStockQuantity() < quantity){
            throw new StockNotAvailableException(" Stock Not Available for the product : "+ id);
        }
        productRepository.updateStockQuantity(id,product.getStockQuantity() - quantity);
        log.info("Successfully reduced the stock for the product {}", id);
    }


    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    @Transactional
    public void reduceStock(OrderCreatedEvent orderCreatedEvent) {

        Long orderId = orderCreatedEvent.orderId();

        if(inventoryReservationRepository.existsByOrderId(orderId)){
            log.info("Inventory reservation already exists for order id: {}", orderId);
            return;
        }

        List<InventoryReservedEvent.ReservedItems> reservedItemsList = new ArrayList<>();

        InventoryReservation inventoryReservation = new InventoryReservation();
        inventoryReservation.setOrderId(orderCreatedEvent.orderId());
        inventoryReservation.setReservationStatus(ReservationStatus.RESERVED);

        orderCreatedEvent.orderItemDto().forEach(orderItemDto -> {
            reduceStock(orderItemDto.productId(), orderItemDto.quantity());
            ReservedItems reservedItem = new ReservedItems();
            reservedItem.setProductId(orderItemDto.productId());
            reservedItem.setQuantity(orderItemDto.quantity());
            inventoryReservation.addReservedItem(reservedItem);
            reservedItemsList.add(new InventoryReservedEvent.ReservedItems(orderItemDto.productId(), orderItemDto.quantity()));
        });

        inventoryReservationRepository.save(inventoryReservation);

        InventoryReservedEvent inventoryReservedEvent = new InventoryReservedEvent(orderCreatedEvent.orderId(), reservedItemsList, orderCreatedEvent.totalAmount());
        inventoryEventProducer.publish(inventoryReservedEvent);
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    @Transactional
    public void restoreStock(PaymentFailedEvent event){
        InventoryReservation inventoryReservation = inventoryReservationRepository.findByOrderId(event.orderId()).orElseThrow(() -> new RuntimeException("Inventory Reserved Entity Not found"));
        if(inventoryReservation.getReservationStatus() != ReservationStatus.RESERVED){
            log.warn("Restore will not happen");
            return;
        }
        List<ReservedItems> reservedItems = inventoryReservation.getReservedItemList();

        Map<Long,Long> productXQuantity = reservedItems.stream().collect(Collectors.toMap(ReservedItems::getProductId,ReservedItems::getQuantity));

        List<Product> products = productRepository.findAllById(productXQuantity.keySet());

        products.forEach(product -> {
            Long quantity = productXQuantity.get(product.getId());
            if( quantity != null){
                product.setStockQuantity(product.getStockQuantity() + quantity);
            }
        });

        productRepository.saveAll(products);
        inventoryReservation.setReservationStatus(ReservationStatus.CANCELLED);
        inventoryReservationRepository.save(inventoryReservation);
        log.info("Restored the Stocks");
    }

    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    public void updateInventoryStatus(PaymentCreatedEvent event){
        InventoryReservation inventoryReservation = inventoryReservationRepository.findByOrderId(event.orderId()).orElseThrow(() -> new RuntimeException("Inventory Reserved Entity Not found"));
        if(inventoryReservation.getReservationStatus() != ReservationStatus.RESERVED){
            log.warn("Update will not happen because of duplicate event");
            return;
        }
        inventoryReservation.setReservationStatus(ReservationStatus.CONFIRMED);
        inventoryReservationRepository.save(inventoryReservation);
        log.info("Inventory status changed to {}", ReservationStatus.CONFIRMED);
    }

}

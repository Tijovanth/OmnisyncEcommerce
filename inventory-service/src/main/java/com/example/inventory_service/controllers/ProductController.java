package com.example.inventory_service.controllers;


import com.example.inventory_service.dtos.ProductCreateRequestDTO;
import com.example.inventory_service.dtos.ProductCreateResponseDTO;
import com.example.inventory_service.dtos.ProductGetResponseDTOs;
import com.example.inventory_service.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductCreateResponseDTO> createProduct(@Valid @RequestBody ProductCreateRequestDTO requestDTO){
        ProductCreateResponseDTO product = productService.createProduct(requestDTO);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    public ResponseEntity<ProductGetResponseDTOs.ProductGetResponseDTO> getProductById(@PathVariable("productId") Long id){
        return new ResponseEntity<>(productService.getProduct(id), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') OR hasRole('CUSTOMER')")
    public ResponseEntity<ProductGetResponseDTOs> getProducts(@Valid @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int perPage,
                                                              @RequestParam(defaultValue = "id") String sortBy,
                                                              @RequestParam(defaultValue = "asc") String sortDirection){
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        return new ResponseEntity<>(productService.getProducts(page, perPage, sortBy, direction), HttpStatus.OK);
    }

    @PutMapping("/reduceStock/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> reduceStock(@PathVariable("productId") Long id, @RequestParam Long currentStock){
        productService.reduceStock(id,currentStock);
        return ResponseEntity.noContent().build();
    }

}

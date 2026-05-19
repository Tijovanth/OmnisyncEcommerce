package com.omnisynce_ecommerce.order_service.feignClients;

import com.omnisynce_ecommerce.order_service.configs.ProductClientConfig;
import com.omnisynce_ecommerce.order_service.dtos.InventoryProductDTO;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", configuration = ProductClientConfig.class)
public interface ProductFeignClient {

    @Retry(name = "productGetRetry")
    @GetMapping("/api/v1/products/{id}")
    InventoryProductDTO getProductById(@PathVariable("id") Long id);

    @PutMapping("/api/v1/products/reduceStock/{id}")
    void reduceStock(@PathVariable("id") Long id, @RequestParam Long currentStock);

}

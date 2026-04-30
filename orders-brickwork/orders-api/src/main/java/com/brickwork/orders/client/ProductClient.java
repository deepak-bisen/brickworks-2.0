package com.brickwork.orders.client;

import com.brickwork.orders.dto.ProductDTO; // we'll need to copy the Phase 2 ProductDTO here!
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// This links directly to the products-brickwork service we built yesterday!
@FeignClient(name = "PRODUCTS-BRICKWORK")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);

    @PutMapping("/api/products/{id}/deduct-stock")
    void deductStock(@PathVariable("id") String id, @RequestParam("quantity") int quantity);
}
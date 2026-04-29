package com.brickwork.orders.client;

import com.brickwork.orders.dto.ProductDTO; // we'll need to copy the Phase 2 ProductDTO here!
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// This links directly to the products-brickwork service we built yesterday!
@FeignClient(name = "PRODUCTS-BRICKWORK")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    ProductDTO getProductById(@PathVariable("id") String id);
}
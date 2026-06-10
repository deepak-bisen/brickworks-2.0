package com.brickwork.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PRODUCTS-BRICKWORK", contextId = "productionClient")
public interface ProductionClient {

    @PostMapping("/api/production-logs/from-order")
    void createFromOrder(@RequestBody ProductionLogFromOrderRequest request);
}
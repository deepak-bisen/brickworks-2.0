package com.brickwork.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "FINANCE-BRICKWORK")
public interface FinanceClient {

    @PostMapping("/api/finance/invoice/generate/{orderId}")
    ResponseEntity<String> generateInvoice(@PathVariable("orderId") String orderId);
}
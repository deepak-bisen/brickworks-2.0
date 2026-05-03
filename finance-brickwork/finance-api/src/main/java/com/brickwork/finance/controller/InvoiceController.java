package com.brickwork.finance.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/finance/invoice")
public interface InvoiceController {

    @PostMapping("/generate/{orderId}")
    ResponseEntity<String> generateInvoice(@PathVariable("orderId") String orderId);

    @GetMapping("/download/{orderId}")
    ResponseEntity<byte[]> downloadInvoice(@PathVariable("orderId") String orderId);
}
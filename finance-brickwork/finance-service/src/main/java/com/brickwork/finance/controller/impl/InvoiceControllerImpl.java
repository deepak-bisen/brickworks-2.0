package com.brickwork.finance.controller.impl;

import com.brickwork.finance.service.PdfInvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class InvoiceControllerImpl {

    @Autowired
    private PdfInvoiceService pdfInvoiceService;

    @Override
    public ResponseEntity<byte[]> testGenerateInvoice(String orderId) {

        // --- MOCK DATA FOR PDF TESTING ---
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceNumber", "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        data.put("invoiceDate", LocalDate.now().toString());
        data.put("customerName", "M/S Apex Builders");
        data.put("customerAddress", "Plot 45, MIDC, Nagpur");
        data.put("customerGst", "27ABCD1234E1Z9");
        data.put("orderId", orderId);

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("productName", "Premium Red Clay Brick");
        item1.put("quantity", 5000);
        item1.put("unitPrice", 8.50);
        item1.put("totalAmount", 5000 * 8.50);
        items.add(item1);

        data.put("items", items);

        double subtotal = 5000 * 8.50;
        double tax = subtotal * 0.18;
        data.put("subtotal", subtotal);
        data.put("taxAmount", tax);
        data.put("totalAmount", subtotal + tax);

        // Generate PDF
        byte[] pdfBytes = pdfInvoiceService.generatePdfFromHtml("invoice", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Invoice_" + orderId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
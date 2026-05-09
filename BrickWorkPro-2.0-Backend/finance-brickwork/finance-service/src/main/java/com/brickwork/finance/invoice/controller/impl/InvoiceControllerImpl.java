package com.brickwork.finance.invoice.controller.impl;

import com.brickwork.finance.invoice.controller.InvoiceController;
import com.brickwork.finance.invoice.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceControllerImpl implements InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    @Override
    public ResponseEntity<String> generateInvoice(String orderId) {
        String invoiceNum = invoiceService.generateAndSaveInvoice(orderId);
        return ResponseEntity.ok("Invoice Generated: " + invoiceNum);
    }

    @Override
    public ResponseEntity<byte[]> downloadInvoice(String orderId) {
        byte[] pdfBytes = invoiceService.getInvoicePdfBytes(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "Invoice_" + orderId + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
package com.brickwork.finance.invoice.controller.impl;

import com.brickwork.finance.invoice.controller.InvoiceController;
import com.brickwork.finance.invoice.exception.InvoiceNotFoundException;
import com.brickwork.finance.invoice.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceControllerImpl implements InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceControllerImpl.class);

    @Autowired
    private InvoiceService invoiceService;

    @Override
    public ResponseEntity<String> generateInvoice(String orderId) {
        try {
            String invoiceNum = invoiceService.generateAndSaveInvoice(orderId);
            return ResponseEntity.ok("Invoice Generated: " + invoiceNum);
        } catch (Exception ex) {
            log.error("Invoice generation failed for order {}", orderId, ex);
            String msg = "{\"error\":\"" + ex.getMessage().replaceAll("\"","'") + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(msg);
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadInvoice(String orderId) {
        try {
            byte[] pdfBytes = invoiceService.getInvoicePdfBytes(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Invoice_" + orderId + ".pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (InvoiceNotFoundException ex) {
            String msg = "{\"error\":\"" + ex.getMessage().replaceAll("\"","'") + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(msg.getBytes());
        } catch (Exception ex) {
            log.error("Invoice download failed for order {}", orderId, ex);
            String msg = "{\"error\":\"" + ex.getMessage().replaceAll("\"","'") + "\"}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).headers(headers).body(msg.getBytes());
        }
    }
}
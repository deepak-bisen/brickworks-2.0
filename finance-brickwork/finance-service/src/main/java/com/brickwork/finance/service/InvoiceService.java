package com.brickwork.finance.service;

public interface InvoiceService {
    String generateAndSaveInvoice(String orderId);
    byte[] getInvoicePdfBytes(String orderId);
}
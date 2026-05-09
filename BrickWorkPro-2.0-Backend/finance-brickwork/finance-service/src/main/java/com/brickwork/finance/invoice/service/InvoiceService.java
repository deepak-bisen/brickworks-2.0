package com.brickwork.finance.invoice.service;

public interface InvoiceService {
    String generateAndSaveInvoice(String orderId);
    byte[] getInvoicePdfBytes(String orderId);
}
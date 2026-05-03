package com.brickwork.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDTO {
    private String invoiceNumber;
    private String orderId;
    private Double totalAmount;
    private LocalDateTime generatedDate;
}
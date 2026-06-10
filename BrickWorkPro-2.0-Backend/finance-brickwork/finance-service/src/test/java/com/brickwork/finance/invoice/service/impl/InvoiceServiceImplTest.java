package com.brickwork.finance.invoice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoiceServiceImplTest {

    private InvoiceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InvoiceServiceImpl();
    }

    @Test
    void normalizeOrderData_mapsItemsAndLineTotals() {
        Map<String, Object> raw = Map.of(
                "orderId", "order-1",
                "totalAmount", 1180,
                "items", List.of(
                        Map.of("productName", "Red Brick", "quantity", 500, "unitPrice", 2.0)
                )
        );

        Map<String, Object> normalized = service.normalizeOrderData(raw);

        assertEquals("order-1", normalized.get("orderId"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) normalized.get("items");
        assertEquals(1, items.size());
        assertEquals("Red Brick", items.get(0).get("productName"));
        assertEquals(500, items.get(0).get("quantity"));
        assertEquals(1000.0, items.get(0).get("lineTotal"));
    }

    @Test
    void normalizeOrderData_handlesMissingItems() {
        Map<String, Object> normalized = service.normalizeOrderData(Map.of("totalAmount", "590"));

        assertEquals(590.0, service.asDouble(normalized.get("totalAmount")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) normalized.get("items");
        assertTrue(items.isEmpty());
    }
}
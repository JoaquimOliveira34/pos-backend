package com.example.demo;

public record InvoiceItem(
        int id,
        String name,
        int quantity,
        double unitPrice) {
    public double getTotal() {
        return quantity * unitPrice;
    }
}
package com.example.cryptotrader.dto;

public class HistoricalPriceDto {

    private long timestamp;
    private double price;

    public HistoricalPriceDto(long timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getPrice() {
        return price;
    }
}


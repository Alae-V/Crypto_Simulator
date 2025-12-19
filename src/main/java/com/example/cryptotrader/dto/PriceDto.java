package com.example.cryptotrader.dto;

public class PriceDto {

    private String name;
    private double priceUsd;

    public PriceDto(String name, double priceUsd) {
        this.name = name;
        this.priceUsd = priceUsd;
    }

    public String getName() {
        return name;
    }

    public double getPriceUsd() {
        return priceUsd;
    }
}

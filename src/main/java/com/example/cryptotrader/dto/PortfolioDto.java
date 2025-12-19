package com.example.cryptotrader.dto;

import java.util.Map;

public class PortfolioDto {
    private double balance;
    private Map<String, Double> holdings;

    public PortfolioDto(double balance, Map<String, Double> holdings) {
        this.balance = balance;
        this.holdings = holdings;
    }

    public double getBalance() {
        return balance;
    }

    public Map<String, Double> getHoldings() {
        return holdings;
    }
}
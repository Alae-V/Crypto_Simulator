package com.example.cryptotrader.dto;

import java.util.Map;

public class CoinGeckoPriceResponse {

    private Map<String, Map<String, Double>> prices;

    public Map<String, Map<String, Double>> getPrices() {
        return prices;
    }

    public void setPrices(Map<String, Map<String, Double>> prices) {
        this.prices = prices;
    }
}

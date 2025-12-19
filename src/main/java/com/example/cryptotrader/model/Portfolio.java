package com.example.cryptotrader.model;

import java.util.HashMap;
import java.util.Map;

public class Portfolio {

    private double balance;
    private final Map<String, Double> holdings = new HashMap<>();

    public Portfolio(double balance) {
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }

    public Map<String, Double> getHoldings() {
        return holdings;
    }

    public void withdraw(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("Not enough balance");
        }
        balance -= amount;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void addCoin(String coin, double amount) {
        holdings.merge(coin, amount, Double::sum);
    }

    public void removeCoin(String coin, double amount) {
        double current = holdings.getOrDefault(coin, 0.0);
        if (amount > current) {
            throw new IllegalArgumentException("Not enough coins");
        }
        if (amount == current) {
            holdings.remove(coin);
        } else {
            holdings.put(coin, current - amount);
        }
    }
}

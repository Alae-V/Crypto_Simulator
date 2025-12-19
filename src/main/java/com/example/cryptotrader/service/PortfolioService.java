package com.example.cryptotrader.service;

import com.example.cryptotrader.dto.PortfolioDto;
import com.example.cryptotrader.model.Portfolio;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PortfolioService {

    private final Portfolio portfolio = new Portfolio(10_000.0);
    private final PriceService priceService;

    public PortfolioService(PriceService priceService) {
        this.priceService = priceService;
    }

    public PortfolioDto getPortfolioDto() {
        return new PortfolioDto(
                portfolio.getBalance(),
                portfolio.getHoldings()
        );
    }

    public void buy(String coin, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        double price = priceService.getPriceForCoin(coin);
        double cost = price * amount;

        if (cost > portfolio.getBalance()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        portfolio.withdraw(cost);
        portfolio.addCoin(coin.toLowerCase(), amount);
    }

    public void sell(String coin, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        double price = priceService.getPriceForCoin(coin);

        try {
            portfolio.removeCoin(coin.toLowerCase(), amount);
            portfolio.deposit(price * amount);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Insufficient coins");
        }
    }
}
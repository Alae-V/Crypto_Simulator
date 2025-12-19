package com.example.cryptotrader.controller;

import com.example.cryptotrader.dto.TradeRequest;
import com.example.cryptotrader.dto.PortfolioDto;
import com.example.cryptotrader.service.PortfolioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<PortfolioDto> getPortfolio() {
        return ResponseEntity.ok(portfolioService.getPortfolioDto());
    }

    @PostMapping("/buy")
    public ResponseEntity<?> buy(@RequestBody TradeRequest request) {
        try {
            portfolioService.buy(request.getCoin(), request.getAmount());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sell(@RequestBody TradeRequest request) {
        try {
            portfolioService.sell(request.getCoin(), request.getAmount());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
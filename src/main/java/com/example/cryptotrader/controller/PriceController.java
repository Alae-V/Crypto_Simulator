package com.example.cryptotrader.controller;

import com.example.cryptotrader.dto.HistoricalPriceDto;
import com.example.cryptotrader.dto.PriceDto;
import com.example.cryptotrader.service.PriceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }


    @GetMapping("/coins")
    public List<String> getSupportedCoins() {
        return priceService.getSupportedCoins();
    }

    @GetMapping("/history")
    public List<HistoricalPriceDto> getHistoricalPrices(
            @RequestParam String coin,
            @RequestParam(defaultValue = "7d") String period) {

        // Validiere period Parameter
        if (!period.equals("7d") && !period.equals("14d") && !period.equals("30d")) {
            period = "7d"; // Default
        }

        return priceService.getHistoricalPrices(coin, period);
    }
    @GetMapping("/prices")
    public List<PriceDto> getPrices() {
        return priceService.getPricesSorted(); // Sortierte Liste zurückgeben
    }
}
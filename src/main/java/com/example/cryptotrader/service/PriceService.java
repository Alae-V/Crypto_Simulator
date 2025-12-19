package com.example.cryptotrader.service;

import com.example.cryptotrader.dto.HistoricalPriceDto;
import com.example.cryptotrader.dto.PriceDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@EnableScheduling
public class PriceService {

    // Weniger Coins für weniger API Calls
    private static final List<String> POPULARITY_ORDER = Arrays.asList(
            "bitcoin", "ethereum", "solana", "dogecoin",
            "cardano", "ripple", "polkadot", "chainlink",
            "litecoin", "stellar", "uniswap"
    );

    private static final Map<String, String> COIN_DISPLAY_NAMES = new HashMap<String, String>() {{
        put("bitcoin", "Bitcoin");
        put("ethereum", "Ethereum");
        put("solana", "Solana");
        put("dogecoin", "Dogecoin");
        put("cardano", "Cardano");
        put("ripple", "Ripple");
        put("polkadot", "Polkadot");
        put("chainlink", "Chainlink");
        put("litecoin", "Litecoin");
        put("stellar", "Stellar");
        put("uniswap", "Uniswap");
    }};

    private final AtomicReference<List<PriceDto>> currentPrices = new AtomicReference<>(new ArrayList<>());
    private final Map<String, Map<String, List<HistoricalPriceDto>>> historicalCache = new ConcurrentHashMap<>();
    private Instant lastPriceFetch = Instant.EPOCH;
    private boolean isFetching = false;
    private int apiCallCounter = 0;
    private static final int MAX_API_CALLS_PER_MINUTE = 30;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PriceService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public List<PriceDto> getPricesSorted() {
        return getPrices();
    }

    public List<PriceDto> getPrices() {
        // Prüfe Rate Limit
        if (apiCallCounter >= MAX_API_CALLS_PER_MINUTE) {
            System.out.println("⚠️ Rate Limit erreicht, verwende Cache");
            return currentPrices.get();
        }

        // Wenn schon in den letzten 60 Sekunden geholt, return cached
        if (Instant.now().minusSeconds(60).isBefore(lastPriceFetch)) {
            return currentPrices.get();
        }

        if (!isFetching) {
            isFetching = true;
            try {
                fetchAndUpdatePrices();
            } finally {
                isFetching = false;
            }
        }

        return currentPrices.get();
    }

    private synchronized void fetchAndUpdatePrices() {
        try {
            apiCallCounter++;

            System.out.println("🔄 Fetching prices (API call #" + apiCallCounter + ")...");

            String coinIds = String.join(",", POPULARITY_ORDER);
            String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coinIds + "&vs_currencies=usd";

            // Rate Limit Schutz
            Thread.sleep(1000); // 1 Sekunde warten zwischen API Calls

            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.trim().isEmpty()) {
                System.err.println("❌ Empty response");
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> data = objectMapper.readValue(response, Map.class);

            List<PriceDto> newPrices = new ArrayList<>();

            for (String coinId : POPULARITY_ORDER) {
                String displayName = COIN_DISPLAY_NAMES.get(coinId);

                if (!data.containsKey(coinId)) {
                    System.err.println("⚠️ Coin not in response: " + coinId);
                    continue;
                }

                Map<String, Object> coinData = data.get(coinId);
                Object usdValue = coinData.get("usd");

                if (usdValue == null) {
                    System.err.println("⚠️ No USD price for " + coinId);
                    continue;
                }

                try {
                    double price = convertToDouble(usdValue);

                    if (price <= 0) {
                        continue;
                    }

                    if (!isPriceRealistic(coinId, price)) {
                        continue;
                    }

                    newPrices.add(new PriceDto(displayName, price));

                } catch (Exception e) {
                    System.err.println("❌ Error parsing price for " + coinId);
                }
            }

            if (!newPrices.isEmpty()) {
                currentPrices.set(sortPricesByPopularity(newPrices));
                lastPriceFetch = Instant.now();
                System.out.println("✅ Updated " + newPrices.size() + " prices");
            }

        } catch (Exception e) {
            System.err.println("❌ Error fetching prices: " + e.getMessage());
        }
    }

    private List<PriceDto> sortPricesByPopularity(List<PriceDto> prices) {
        if (prices == null || prices.isEmpty()) {
            return new ArrayList<>();
        }

        List<PriceDto> sorted = new ArrayList<>(prices);

        sorted.sort((a, b) -> {
            String aId = getCoinIdFromName(a.getName());
            String bId = getCoinIdFromName(b.getName());

            int aIndex = POPULARITY_ORDER.indexOf(aId);
            int bIndex = POPULARITY_ORDER.indexOf(bId);

            if (aIndex != -1 && bIndex != -1) {
                return Integer.compare(aIndex, bIndex);
            }

            if (aIndex != -1) return -1;
            if (bIndex != -1) return 1;

            return a.getName().compareTo(b.getName());
        });

        return sorted;
    }

    private String getCoinIdFromName(String name) {
        for (Map.Entry<String, String> entry : COIN_DISPLAY_NAMES.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return name.toLowerCase();
    }

    private double convertToDouble(Object value) {
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Double) return (Double) value;
        if (value instanceof Long) return ((Long) value).doubleValue();
        if (value instanceof Float) return ((Float) value).doubleValue();
        if (value instanceof String) return Double.parseDouble((String) value);
        return 0.0;
    }

    private boolean isPriceRealistic(String coinId, double price) {
        switch (coinId) {
            case "bitcoin": return price > 1000 && price < 200000;
            case "ethereum": return price > 10 && price < 20000;
            case "solana": return price > 1 && price < 1000;
            case "dogecoin": return price > 0.0001 && price < 10;
            default: return price > 0.0001 && price < 100000;
        }
    }

    public List<HistoricalPriceDto> getHistoricalPrices(String coinId, String period) {
        String normalizedCoinId = coinId.toLowerCase();

        if (!POPULARITY_ORDER.contains(normalizedCoinId)) {
            return Collections.emptyList();
        }

        // Cache prüfen (3 Minuten Cache für Charts)
        String cacheKey = normalizedCoinId + "_" + period;
        if (historicalCache.containsKey(normalizedCoinId) &&
                historicalCache.get(normalizedCoinId) != null &&
                historicalCache.get(normalizedCoinId).containsKey(period)) {
            return historicalCache.get(normalizedCoinId).get(period);
        }

        // Rate Limit prüfen
        if (apiCallCounter >= MAX_API_CALLS_PER_MINUTE) {
            System.out.println("⚠️ Rate Limit, kein Chart-Daten-Fetch");
            return Collections.emptyList();
        }

        try {
            apiCallCounter++;
            System.out.println("📊 Fetching chart (API call #" + apiCallCounter + ")...");

            List<HistoricalPriceDto> data = fetchHistoricalData(normalizedCoinId, period);

            if (!data.isEmpty()) {
                historicalCache.computeIfAbsent(normalizedCoinId, k -> new HashMap<>())
                        .put(period, data);
            }

            return data;

        } catch (Exception e) {
            System.err.println("Error fetching historical: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<HistoricalPriceDto> fetchHistoricalData(String coinId, String period) {
        int days = getDaysFromPeriod(period);

        try {
            String url = "https://api.coingecko.com/api/v3/coins/" + coinId +
                    "/market_chart?vs_currency=usd&days=" + days;

            // Rate Limit Schutz
            Thread.sleep(2000); // 2 Sekunden warten für Charts

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(response, Map.class);

            @SuppressWarnings("unchecked")
            List<List<Number>> prices = (List<List<Number>>) data.get("prices");

            if (prices == null || prices.isEmpty()) {
                return Collections.emptyList();
            }

            List<HistoricalPriceDto> result = new ArrayList<>();
            int step = Math.max(1, prices.size() / 50);

            for (int i = 0; i < prices.size(); i += step) {
                List<Number> point = prices.get(i);
                if (point.size() >= 2) {
                    long timestamp = point.get(0).longValue();
                    double price = point.get(1).doubleValue();

                    if (price > 0) {
                        result.add(new HistoricalPriceDto(timestamp, price));
                    }
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("API error for " + coinId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private int getDaysFromPeriod(String period) {
        switch (period.toLowerCase()) {
            case "14d": return 14;
            case "30d": return 30;
            default: return 7;
        }
    }

    public double getPriceForCoin(String coinName) {
        String normalizedName = coinName.toLowerCase();

        for (PriceDto price : currentPrices.get()) {
            if (price.getName().equalsIgnoreCase(normalizedName) ||
                    price.getName().toLowerCase().contains(normalizedName)) {
                return price.getPriceUsd();
            }
        }

        throw new IllegalArgumentException("Coin not found: " + coinName);
    }

    public List<String> getSupportedCoins() {
        return new ArrayList<>(POPULARITY_ORDER);
    }

    @Scheduled(fixedRate = 60000) // Alle 60 Sekunden
    public void refreshPrices() {
        if (!isFetching && apiCallCounter < MAX_API_CALLS_PER_MINUTE) {
            fetchAndUpdatePrices();
        }
    }

    // Reset Rate Limit Counter jede Minute
    @Scheduled(fixedRate = 60000)
    public void resetApiCounter() {
        apiCallCounter = 0;
        System.out.println("🔄 API Counter reset");
    }
}
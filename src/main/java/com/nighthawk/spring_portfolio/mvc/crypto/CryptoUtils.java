package com.nighthawk.spring_portfolio.mvc.crypto;

import java.util.*;
import java.util.stream.Collectors;

public class CryptoUtils {

    public static String sanitizeCryptoHoldings(String rawHoldings) {
        if (rawHoldings == null || rawHoldings.trim().isEmpty()) return "";

        Map<String, Double> holdings = new TreeMap<>();
        for (String entry : rawHoldings.split(",")) {
            String[] parts = entry.trim().split(":");
            if (parts.length != 2) continue;

            String symbol = parts[0].trim();
            String amountStr = parts[1].trim();

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0 || symbol.isEmpty()) continue;
                holdings.put(symbol, holdings.getOrDefault(symbol, 0.0) + amount);
            } catch (NumberFormatException e) {
                continue;
            }
        }

        return holdings.entrySet().stream()
            .map(e -> e.getKey() + ":" + String.format("%.8f", e.getValue()))
            .collect(Collectors.joining(","));
    }
}
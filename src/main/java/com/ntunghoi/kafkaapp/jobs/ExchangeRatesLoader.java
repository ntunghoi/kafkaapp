package com.ntunghoi.kafkaapp.jobs;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntunghoi.kafkaapp.services.ExchangeRatesService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Component
public class ExchangeRatesLoader {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesLoader.class);

    @Value("${exchange-rate.url}")
    private String url;

    @Value("${exchange-rate.currency-codes}")
    private String delimitedCurrencyCodes;
    private Set<String> currencyCodes;
    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public ExchangeRatesLoader(
            ExchangeRatesService exchangeRatesService
    ) {
        this.exchangeRatesService = exchangeRatesService;
    }

    @PostConstruct
    private void init() {
        currencyCodes = Set.of(
                delimitedCurrencyCodes.split(",")
        );
        invokeApi();
    }

    @Scheduled(cron = "${exchange-rate.cron}")
    public void fetchExchangeRates() {
        logger.info("Fetch exchange rate now ...");
        invokeApi();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ResponseBody(
            @JsonProperty("result")
            String result,
            @JsonProperty("time_last_update_unix")
            long lastUpdateTimestamp,
            @JsonProperty("base_code")
            String baseCurrencyCode,
            @JsonProperty("conversion_rates")
            Map<String, String> conversionRates
    ) {}

    private void invokeApi() {
        logger.info("Calling api ({}) to get exchange rates", url);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::parse)
                .join();
    }

    private void parse(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ResponseBody responseBody = objectMapper.readValue(jsonString, ResponseBody.class);
            Instant instant = Instant.ofEpochMilli(responseBody.lastUpdateTimestamp);
            LocalDateTime updatedAt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            responseBody.conversionRates.forEach((key, value) -> {
                if(currencyCodes.contains(key)) {
                    exchangeRatesService.updateExchangeRate(
                            key,
                            new BigDecimal(value),
                            updatedAt
                    );
                }
            });
        } catch(IOException ioException) {
            logger.error("Error in parsing response data: {}", ioException.getMessage());
        }
    }
}
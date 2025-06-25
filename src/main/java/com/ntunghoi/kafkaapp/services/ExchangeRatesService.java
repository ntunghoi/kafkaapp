package com.ntunghoi.kafkaapp.services;

import com.ntunghoi.kafkaapp.entities.ExchangeRateEntity;
import com.ntunghoi.kafkaapp.exceptions.SystemConfigurationException;
import com.ntunghoi.kafkaapp.models.ExchangeRate;
import com.ntunghoi.kafkaapp.repositories.ExchangeRatesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class ExchangeRatesService {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesService.class);
    private final ExchangeRatesRepository exchangeRatesRepository;

    @Autowired
    public ExchangeRatesService(
            ExchangeRatesRepository exchangeRatesRepository
    ) {
        this.exchangeRatesRepository = exchangeRatesRepository;
    }

    @Cacheable(value = "exchangeRatesCache", key = "#currencyCode")
    public ExchangeRate getExchangeRate(String currencyCode) throws SystemConfigurationException {
        logger.info("Get exchange rate for currency {}", currencyCode);
        return exchangeRatesRepository
                .findByCurrencyCode(currencyCode)
                .orElseThrow(() -> new SystemConfigurationException(String.format("Missing exchange rate for currency code: %s", currencyCode)
                ));
    }

    @CachePut(value = "exchangeRatesCache", key = "#currencyCode")
    public ExchangeRate updateExchangeRate(
            String currencyCode,
            BigDecimal rate,
            LocalDateTime updatedAt
    ) {
        ExchangeRateEntity exchangeRateEntity = new ExchangeRateEntity();
        exchangeRateEntity.setCurrencyCode(currencyCode)
                .setRate(rate)
                .setUpdatedAt(updatedAt);
        exchangeRatesRepository.save(exchangeRateEntity);

        return exchangeRateEntity;
    }
}

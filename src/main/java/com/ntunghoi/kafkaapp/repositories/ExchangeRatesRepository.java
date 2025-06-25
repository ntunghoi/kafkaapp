package com.ntunghoi.kafkaapp.repositories;

import com.ntunghoi.kafkaapp.entities.ExchangeRateEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExchangeRatesRepository
        extends CrudRepository<ExchangeRateEntity, String> {
    Optional<ExchangeRateEntity> findByCurrencyCode(String currencyCode);
}
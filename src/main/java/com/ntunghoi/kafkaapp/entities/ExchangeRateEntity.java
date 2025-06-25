package com.ntunghoi.kafkaapp.entities;


import com.ntunghoi.kafkaapp.models.ExchangeRate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "exchange_rates")
@Entity
public class ExchangeRateEntity implements ExchangeRate {
    @Id
    @Column(name = "currency_code")
    private String currencyCode;

    @Column(name = "rate")
    private BigDecimal rate;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public String getCurrencyCode() {
        return currencyCode;
    }

    @Override
    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public ExchangeRateEntity setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;

        return this;
    }

    public ExchangeRateEntity setRate(BigDecimal rate) {
        this.rate = rate;

        return this;
    }

    public ExchangeRateEntity setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;

        return this;
    }
}

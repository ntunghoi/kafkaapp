package com.ntunghoi.kafkaapp.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ExchangeRate extends Serializable {
    String getCurrencyCode();
    BigDecimal getRate();
    LocalDateTime getUpdatedAt();
}
package com.ntunghoi.kafkaapp.models;

import java.math.BigDecimal;
import java.util.List;

public record PagedResult<T>(
        List<T> data,
        BigDecimal credit,
        BigDecimal debit,
        String currencyCode,
        int size,
        long offset,
        int count
) {
    public PagedResult(String currencyCode) {
        this(
                List.of(),
                BigDecimal.valueOf(0),
                BigDecimal.valueOf(0),
                currencyCode,
                0,
                0L,
                0
        );
    }
}
package com.ntunghoi.kafkaapp.models;

import java.util.List;

public record PagedResult<T>(
        List<T> data,
        int size,
        long next,
        int count,
        int totalPages
) {
}
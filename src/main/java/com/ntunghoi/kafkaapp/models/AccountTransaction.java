package com.ntunghoi.kafkaapp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public record AccountTransaction(
        @JsonProperty("id")
        String id,

        @JsonProperty("amount")
        BigDecimal amount,

        @JsonProperty("currency")
        String currency,

        @JsonProperty("account_number")
        String accountNumber,

        @JsonProperty("value_date")
        long valueDate,

        @JsonProperty("value_timestamp")
        long valueTimestamp,

        @JsonProperty("description")
        String description
) {
    private static final SimpleDateFormat valueTimestampFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final SimpleDateFormat valueDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String toValueDate(Date date) {
        return valueDateFormat.format(date);
    }

    public static Date fromValueDate(String value) throws ParseException {
        return valueDateFormat.parse(value);
    }

    public String valueTimestampAsString() {
        return valueTimestampFormat.format(valueTimestamp);
    }
}

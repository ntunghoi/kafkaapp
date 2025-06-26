package com.ntunghoi.kafkaapp.comands.dataimport;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DataMapper {
    private final static DateTimeFormatter rawDateFormat =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DataImportCommand.RawAccountTransaction fromRaw(JsonNode raw) throws ParseException {
        BigDecimal amount = BigDecimal.valueOf(raw.get("amount").asDouble());
        String type = raw.get("transaction_type").asText();
        String amountWithCurrency = String.format("%s %s",
                type.equals("DEBIT") ? amount.negate() : amount,
                raw.get("currency").asText()
        );
        ZonedDateTime transactionDate = LocalDateTime
                .parse(raw.get("transaction_date").asText(), rawDateFormat)
                .atZone(ZoneId.systemDefault());


        return new DataImportCommand.RawAccountTransaction(
                raw.get("transaction_id").asText(),
                amountWithCurrency,
                transactionDate.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                transactionDate.toInstant().toEpochMilli(),
                raw.get("account_number").asText(),
                raw.get("description").asText()
        );
    }
}

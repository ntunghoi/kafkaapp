package com.ntunghoi.kafkaapp.comands.dataimport;

import com.fasterxml.jackson.databind.JsonNode;
import com.ntunghoi.kafkaapp.models.AccountTransaction;

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

    public static AccountTransaction toAccountTransaction(JsonNode node) {
        String[] tokens = node.get("AMOUNT_WITH_CURRENCY").asText().split(" ");
        BigDecimal amount = new BigDecimal(tokens[0]);
        String currency = tokens[1];

        return new AccountTransaction(
                node.get("TRANSACTION_ID").asText(),
                amount,
                currency,
                node.get("ACCOUNT_NUMBER").asText(),
                node.get("VALUE_DATE").asLong(),
                node.get("VALUE_TIMESTAMP").asLong(),
                node.get("DESCRIPTION").asText()
        );
    }
}

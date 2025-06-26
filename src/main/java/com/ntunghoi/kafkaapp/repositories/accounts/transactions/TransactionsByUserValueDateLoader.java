package com.ntunghoi.kafkaapp.repositories.accounts.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ntunghoi.kafkaapp.comands.dataimport.DataMapper;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import io.confluent.ksql.api.client.Row;

import java.util.List;
import java.util.function.Consumer;

import static com.ntunghoi.kafkaapp.components.KsqlClient.DataLoaderHelper;

public class TransactionsByUserValueDateLoader implements DataLoaderHelper<List<AccountTransaction>> {
    private final int userId;
    private final long startDate;
    private final long endDate;
    private final Consumer<Throwable> errorHandler;

    public TransactionsByUserValueDateLoader(
            int userId,
            long startDate,
            long endDate,
            Consumer<Throwable> errorHandler
    ) {
        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.errorHandler = errorHandler;
    }

    @Override
    public String getQuery() {
        return String.format("""
               SELECT
                    user_id,
                    value_date,
                    daily_transactions
               FROM transactions_by_user_value_date
               WHERE user_id = %s
               AND value_date BETWEEN %d AND %d;
               """, userId, startDate, endDate);
    }

    @Override
    public List<AccountTransaction> parse(Row row) throws Exception {
        Object dailyTransactions = row.getValue("DAILY_TRANSACTIONS");
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode nodes = (ArrayNode) objectMapper.readTree(dailyTransactions.toString());

        return nodes.valueStream().map(DataMapper::toAccountTransaction).toList();
    }

    @Override
    public void onError(Throwable throwable) {
        errorHandler.accept(throwable);
    }
}

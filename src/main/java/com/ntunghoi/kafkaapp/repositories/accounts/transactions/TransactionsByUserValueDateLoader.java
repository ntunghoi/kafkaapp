package com.ntunghoi.kafkaapp.repositories.accounts.transactions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.ntunghoi.kafkaapp.comands.dataimport.DataMapper;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import io.confluent.ksql.api.client.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.ntunghoi.kafkaapp.components.KsqlClient.DataLoaderHelper;

public class TransactionsByUserValueDateLoader implements DataLoaderHelper<AccountTransaction> {
    private static final Logger logger = LoggerFactory.getLogger(TransactionsByUserValueDateLoader.class);
    private final int userId;

    private final String accountNumber;
    private final long startDate;
    private final long endDate;
    private final int size;
    private final Consumer<Throwable> errorHandler;

    private final List<AccountTransaction> items;

    public TransactionsByUserValueDateLoader(
            int userId,
            String accountNumber,
            long startDate,
            long endDate,
            int size,
            Consumer<Throwable> errorHandler
    ) {
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.size = size;
        this.errorHandler = errorHandler;
        this.items = new ArrayList<>();
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
               AND account_number = '%s'
               AND value_date BETWEEN %d AND %d;
               """, userId, accountNumber, startDate, endDate);
    }

    @Override
    public boolean parse(Row row) throws Exception {
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyy-MM-dd");
        System.out.printf("%s%n", simpleDateFormat.format(new Date((Long) row.getValue("VALUE_DATE"))));
        Object dailyTransactions = row.getValue("DAILY_TRANSACTIONS");
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode nodes = (ArrayNode) objectMapper.readTree(dailyTransactions.toString());

        logger.info("items.size(): {}", items.size());
        items.addAll(nodes.valueStream().map(node -> DataMapper.toAccountTransaction(accountNumber, node)).toList());
        items.forEach(item -> logger.info("{} / {}", item.id(), simpleDateFormat.format(item.valueTimestamp())));
        logger.info("item.size: {} / size: {}", items.size(), size);

        return items.size() >= size;
    }

    @Override
    public void onError(Throwable throwable) {
        errorHandler.accept(throwable);
    }

    @Override
    public List<AccountTransaction> getItems() {
        return items;
    }
}

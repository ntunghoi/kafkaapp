package com.ntunghoi.kafkaapp.repositories.accounts.transactions;

import com.ntunghoi.kafkaapp.components.KsqlClient;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountTransactionsRepository {
    private final KsqlClient<List<AccountTransaction>> ksqlClient;

    @Autowired
    public AccountTransactionsRepository(
            KsqlClient<List<AccountTransaction>> ksqlClient
    ) {
        this.ksqlClient = ksqlClient;
    }

    public interface QueryByUserDate {
        int userId();

        long startDate();

        long endDate();
    }

    public List<AccountTransaction> getTransactionsByUserDate(
            QueryByUserDate query
    ) throws Exception {
        return ksqlClient
                .executeQuery(
                        new TransactionsByUserValueDateLoader(
                                query.userId(),
                                query.startDate(),
                                query.endDate(),
                                throwable -> {
                                    System.err.printf("Error occurred: %s%n", throwable.getMessage());
                                    throwable.printStackTrace(System.err);
                                }
                        )
                ).stream().flatMap(List::stream)
                .toList();
    }
}

package com.ntunghoi.kafkaapp.repositories.accounts.transactions;

import com.ntunghoi.kafkaapp.components.KsqlClient;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccountTransactionsRepository {
    private final static Logger logger = LoggerFactory.getLogger(AccountTransactionsRepository.class);
    private final KsqlClient<AccountTransaction> ksqlClient;

    @Autowired
    public AccountTransactionsRepository(
            KsqlClient<AccountTransaction> ksqlClient
    ) {
        this.ksqlClient = ksqlClient;
    }

    public interface QueryByUserDate {
        int userId();

        String accountNumber();

        long startDate();

        long endDate();
        int size();
    }

    public List<AccountTransaction> getTransactionsByUserDate(
            QueryByUserDate query
    ) throws Exception {
        return ksqlClient
                .executeQuery(
                        new TransactionsByUserValueDateLoader(
                                query.userId(),
                                query.accountNumber(),
                                query.startDate(),
                                query.endDate(),
                                query.size(),
                                throwable -> {
                                    logger.error("Error occurred: {}", throwable.getMessage());
                                }
                        )
                ).stream()
                .toList();
    }
}

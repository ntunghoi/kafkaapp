package com.ntunghoi.kafkaapp.services;

import com.ntunghoi.kafkaapp.models.AccountTransaction;
import com.ntunghoi.kafkaapp.models.ExchangeRate;
import com.ntunghoi.kafkaapp.models.PagedResult;
import com.ntunghoi.kafkaapp.repositories.accounts.transactions.AccountTransactionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Service
public class AccountTransactionsService {
    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionsService.class);
    private final AccountTransactionsRepository accountTransactionsRepository;
    private final ExchangeRatesService exchangeRatesService;

    @Autowired
    public AccountTransactionsService(
            AccountTransactionsRepository accountTransactionsRepository,
            ExchangeRatesService exchangeRatesService
    ) {
        this.accountTransactionsRepository = accountTransactionsRepository;
        this.exchangeRatesService = exchangeRatesService;
    }

    public record AccountTransactionsQuery(
            int userId,
            String preferredCurrency,
            long startDate,
            long endDate,
            long next,
            int size
    ) implements AccountTransactionsRepository.QueryByUserDate {
    }

    public void getAccountTransactions(
            AccountTransactionsQuery query,
            Consumer<PagedResult<AccountTransaction>> onData
    ) throws Exception {
        ExchangeRate exchangeRate = exchangeRatesService.getExchangeRate(query.preferredCurrency);

        System.out.printf("Next: %d%n", query.next());
        List<AccountTransaction> allAccountTransactions =
                accountTransactionsRepository
                        .getTransactionsByUserDate(query)
                        .stream()
                        .sorted(Comparator.comparingLong(AccountTransaction::valueTimestamp))
                        .toList();
        System.out.printf("Number of all account transactions: %d%n", allAccountTransactions.size());
        AtomicInteger counter = new AtomicInteger(-1);
        int index = allAccountTransactions
                .stream()
                .filter(accountTransaction -> {
                    counter.getAndIncrement();
                    return accountTransaction.valueTimestamp() > query.next();
                })
                .mapToInt(accountTransaction -> counter.get())
                .findFirst()
                .orElse(-1);

        if(index == -1) {
            onData.accept( new PagedResult<>(
                    List.of(),
                    query.size(),
                    0,
                    0,
                    0
            ));
        } else {
            List<AccountTransaction> accountTransactions = allAccountTransactions.subList(index, Math.min(index + query.size(), allAccountTransactions.size() - 1));
            System.out.printf("Index: %d / accountTransactions.size(): %d%n", index, accountTransactions.size());
            long next = (accountTransactions.size() + index > allAccountTransactions.size())
                    ? -1 : accountTransactions.getLast().valueTimestamp();
            System.out.println("next: " + next);
            onData.accept(
                    (new PagedResult<>(
                            accountTransactions,
                            query.size(),
                            next,
                            accountTransactions.size(),
                            calculateTotalPages(allAccountTransactions.size(), query.size())
                    )));
        }


    }

    private int calculateTotalPages(int total, int pageSize) {
        if (total == 0) {
            return 0;
        }

        return (int) Math.ceil((double) total / pageSize);
    }
}

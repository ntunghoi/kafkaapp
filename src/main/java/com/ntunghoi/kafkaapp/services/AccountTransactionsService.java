package com.ntunghoi.kafkaapp.services;

import com.ntunghoi.kafkaapp.exceptions.SystemConfigurationException;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import com.ntunghoi.kafkaapp.models.ExchangeRate;
import com.ntunghoi.kafkaapp.models.PagedResult;
import com.ntunghoi.kafkaapp.repositories.accounts.transactions.AccountTransactionsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            String accountNumber,
            String preferredCurrency,
            long startDate,
            long endDate,
            long offset,
            int size
    ) implements AccountTransactionsRepository.QueryByUserDate {
    }

    public void getAccountTransactions(
            AccountTransactionsQuery query,
            Consumer<PagedResult<AccountTransaction>> onData
    ) throws Exception {
        List<AccountTransaction> allAccountTransactions =
                accountTransactionsRepository
                        .getTransactionsByUserDate(query)
                        .stream()
                        .sorted(Comparator.comparingLong(AccountTransaction::valueTimestamp))
                        .toList();
        AtomicInteger counter = new AtomicInteger(-1);
        int index = allAccountTransactions
                .stream()
                .filter(accountTransaction -> {
                    counter.getAndIncrement();
                    return accountTransaction.valueTimestamp() > query.offset();
                })
                .mapToInt(accountTransaction -> counter.get())
                .findFirst()
                .orElse(-1);

        if(index == -1) {
            onData.accept( new PagedResult<>(query.preferredCurrency));
        } else {
            List<AccountTransaction> accountTransactions = allAccountTransactions.subList(index, Math.min(index + query.size(), allAccountTransactions.size()));
            long next = (accountTransactions.size() + index > allAccountTransactions.size())
                    ? -1 : accountTransactions.getLast().valueTimestamp();
            onData.accept(prepare(query.preferredCurrency, accountTransactions, query.size(), next));
        }
    }

    private PagedResult<AccountTransaction> prepare(
            String preferredCurrency,
            List<AccountTransaction> accountTransactions,
            int size,
            long offset
    ) throws SystemConfigurationException{
        BigDecimal totalCredit = BigDecimal.valueOf(0);
        BigDecimal totalDebit = BigDecimal.valueOf(0);

        for(AccountTransaction accountTransaction: accountTransactions) {
            BigDecimal amount = convert(accountTransaction.amount(), accountTransaction.currency(), preferredCurrency);
            if(amount.compareTo(BigDecimal.ZERO) > 0) {
                totalCredit = totalCredit.add(amount);
            } else if(amount.compareTo(BigDecimal.ZERO) < 0) {
                totalDebit = totalDebit.add(amount);
            }
        }

        return new PagedResult<>(
                accountTransactions,
                totalCredit,
                totalDebit,
                preferredCurrency,
                size,
                offset,
                accountTransactions.size()
        );
    }

    private BigDecimal convert(BigDecimal amount, String sourceCurrencyCode, String targetCurrencyCode) throws SystemConfigurationException {
        if(sourceCurrencyCode.equals(targetCurrencyCode)) {
            return amount;
        }

        ExchangeRate sourceExchangeRate = exchangeRatesService.getExchangeRate(sourceCurrencyCode);
        ExchangeRate targetExchangeRate = exchangeRatesService.getExchangeRate(targetCurrencyCode);
        return amount.divide(sourceExchangeRate.getRate(), 4, RoundingMode.HALF_UP).multiply(targetExchangeRate.getRate());
    }
}

package com.ntunghoi.kafkaapp;


import com.ntunghoi.kafkaapp.models.AccountTransaction;
import com.ntunghoi.kafkaapp.models.ExchangeRate;
import com.ntunghoi.kafkaapp.models.PagedResult;
import com.ntunghoi.kafkaapp.repositories.accounts.transactions.AccountTransactionsRepository;
import com.ntunghoi.kafkaapp.services.AccountTransactionsService;
import com.ntunghoi.kafkaapp.services.ExchangeRatesService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static com.ntunghoi.kafkaapp.services.AccountTransactionsService.AccountTransactionsQuery;

@ExtendWith(MockitoExtension.class)
public class AccountTransactionServiceUnitTest {
    @Mock
    private AccountTransactionsRepository accountTransactionsRepository;

    @Mock
    private ExchangeRatesService exchangeRatesService;

    @Mock
    private ExchangeRate exchangeRate;

    @Mock
    private ExchangeRate eur2Eur;

    @Mock
    private ExchangeRate gbp2Eur;

    @InjectMocks
    private AccountTransactionsService accountTransactionsService;

    @Test
    void test_GetAccountTransactions_NoData() throws Exception {
        AccountTransactionsQuery query = new AccountTransactionsQuery(
                1,
                "",
                "",
                0,
                0,
                10,
                0
        );

        Consumer<PagedResult<AccountTransaction>> onData = result -> {
            assertThat(result).isNotNull();
            assertThat(result.data().size()).isEqualTo(0);
            assertThat(result.count()).isEqualTo(0);
        };

        when(accountTransactionsRepository.getTransactionsByUserDate(query))
                .thenReturn(new ArrayList<>());
        accountTransactionsService.getAccountTransactions(
                query,
                onData
        );
    }

    @Nested
    class AccountTransactionRetriever {
        private final int size = 20;
        private final List<AccountTransaction> expectedAccountTransactions;
        private final ArrayList<AccountTransaction> actualAccountTransactions = new ArrayList<>();
        private boolean isDone = false;

        private AccountTransactionsQuery query = new AccountTransactionsQuery(
                1,
                "GB84CPBK83157663644901",
                "EUR",
                0,
                0,
                0,
                size
        );

        AccountTransactionRetriever() {
            DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String rawData = """
                    96ED1D76,GB84CPBK83157663644901,2024-01-08 01:38:14,865.55,GBP,TRANSFER FROM ACCOUNT ***4462
                    0AD73675,GB84CPBK83157663644901,2024-01-08 13:28:52,7294.48,EUR,INTEREST CREDIT
                    A072AFDB,GB84CPBK83157663644901,2024-01-09 08:53:31,44609.93,EUR,INTEREST CREDIT
                    FB020072,GB84CPBK83157663644901,2024-01-08 17:00:06,-3586.22,GBP,Visa - PURCHASE
                    5A99385A,GB84CPBK83157663644901,2024-01-08 03:16:54,39861.24,EUR,Freelance
                    1FCBA917,GB84CPBK83157663644901,2024-01-08 11:02:05,-10625.72,EUR,Restaurant
                    C1B94143,GB84CPBK83157663644901,2024-01-08 19:53:00,-1281.35,EUR,Rent
                    B2D3D859,GB84CPBK83157663644901,2024-01-08 10:03:58,29496.13,EUR,INTEREST CREDIT
                    215E9DEC,GB84CPBK83157663644901,2024-01-08 21:17:44,-4354.52,GBP,Bill Payment
                    143D4E1B,GB84CPBK83157663644901,2024-01-08 03:47:13,-5394.1,EUR,ATM WITHDRAWAL - STREET
                    262CB9A2,GB84CPBK83157663644901,2024-01-09 22:36:22,7221.16,GBP,INTEREST CREDIT
                    6632A0D2,GB84CPBK83157663644901,2024-01-09 15:16:58,-3858.66,GBP,TRANSFER TO ACCOUNT ***3238
                    48E5EBF4,GB84CPBK83157663644901,2024-01-09 07:32:12,3691.61,EUR,Dividend
                    E56F4880,GB84CPBK83157663644901,2024-01-09 19:32:39,-391.26,EUR,TRANSFER TO ACCOUNT ***2995
                    D49BA27C,GB84CPBK83157663644901,2024-01-09 17:08:46,-3728.74,EUR,Walmart - ONLINE_PURCHASE
                    71F03D61,GB84CPBK83157663644901,2024-01-09 01:32:49,1056.13,GBP,Bonus
                    87118AEA,GB84CPBK83157663644901,2024-01-09 22:09:08,36055.63,GBP,Deposit
                    1BA16563,GB84CPBK83157663644901,2024-01-09 17:47:47,-9819.82,EUR,Insurance
                    32CC0C40,GB84CPBK83157663644901,2024-01-09 03:41:06,886.78,EUR,Deposit
                    CF6CBA25,GB84CPBK83157663644901,2024-01-08 19:46:09,-3916.96,EUR,Restaurant
                    51FAF2B5,GB84CPBK83157663644901,2024-01-08 04:44:15,-5507.2,GBP,Bill Payment
                    4B6159AC,GB84CPBK83157663644901,2024-01-08 14:35:07,30957.09,EUR,Freelance
                    0E848758,GB84CPBK83157663644901,2024-01-08 18:34:01,2866.19,GBP,Dividend
                    F76D55B7,GB84CPBK83157663644901,2024-01-08 22:22:48,-1502.3,EUR,Subscription
                    F9614335,GB84CPBK83157663644901,2024-01-08 01:19:34,-21472.28,EUR,ATM WITHDRAWAL - STREET
                    804E85DB,GB84CPBK83157663644901,2024-01-08 11:31:52,18329.2,EUR,Refund
                    """;
            expectedAccountTransactions = Arrays.stream(rawData.split("\n")).map(line -> {
                String[] tokens = line.split(",");
                ZonedDateTime transactionDate = LocalDateTime
                        .parse(tokens[2], DATE_TIME_FORMATTER)
                        .atZone(ZoneId.systemDefault());

                return new AccountTransaction(
                        tokens[0],
                        new BigDecimal(tokens[3]),
                        tokens[4],
                        tokens[1],
                        transactionDate.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        transactionDate.toInstant().toEpochMilli(),
                        tokens[5]
                );
            }).collect(Collectors.toList());
        }

        private static final List<String> sortedTransactionIds = List.of(
                "F9614335",
                "96ED1D76",
                "5A99385A",
                "143D4E1B",
                "51FAF2B5",
                "B2D3D859",
                "1FCBA917",
                "804E85DB",
                "0AD73675",
                "4B6159AC",
                "FB020072",
                "0E848758",
                "CF6CBA25",
                "C1B94143",
                "215E9DEC",
                "F76D55B7",
                "71F03D61",
                "32CC0C40",
                "48E5EBF4",
                "A072AFDB",
                "6632A0D2",
                "D49BA27C",
                "1BA16563",
                "E56F4880",
                "87118AEA",
                "262CB9A2"
        );

        private void onData(PagedResult<AccountTransaction> result) {
            actualAccountTransactions.addAll(result.data());
            assertThat(result).isNotNull();
            assertThat(result.data().size()).isLessThanOrEqualTo(size);
            for (int index = 1; index < result.data().size(); index++) {
                long lastValueTimestamp = result.data().get(index - 1).valueTimestamp();
                assertThat(result.data().get(index).valueTimestamp()).isGreaterThanOrEqualTo(lastValueTimestamp);
            }
            result.data().forEach(accountTransaction -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                System.out.printf("%s/ %s%n", accountTransaction.id(), simpleDateFormat.format(new Date(accountTransaction.valueTimestamp())));
            });
            System.out.printf("%s%n", new Date(result.offset()));
            isDone = result.data().isEmpty() || result.data().size() < result.size();
            if (!isDone) {
                query = new AccountTransactionsQuery(
                        query.userId(),
                        query.accountNumber(),
                        query.preferredCurrency(),
                        query.startDate(),
                        query.endDate(),
                        result.offset(),
                        query.size()
                );
            }
        }

        @Test
        void test_GetAccountTransactions() throws Exception {
            while (!isDone) {
                when(eur2Eur.getRate()).thenReturn(BigDecimal.valueOf(1.0));
                when(gbp2Eur.getRate()).thenReturn(BigDecimal.valueOf(0.8531));
                when(exchangeRatesService.getExchangeRate("EUR")).thenReturn(eur2Eur);
                when(exchangeRatesService.getExchangeRate("GBP")).thenReturn(gbp2Eur);
                when(accountTransactionsRepository.getTransactionsByUserDate(query))
                        .thenReturn(expectedAccountTransactions);

                accountTransactionsService.getAccountTransactions(
                        query,
                        this::onData
                );
            }
            assertThat(actualAccountTransactions.size()).isEqualTo(expectedAccountTransactions.size());
            for (int index = 0; index < actualAccountTransactions.size(); index++) {
                assertThat(actualAccountTransactions.get(index).id()).isEqualTo(sortedTransactionIds.get(index));
            }
        }
    }


}

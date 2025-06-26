package com.ntunghoi.kafkaapp.controllers.account.transactions;

import com.ntunghoi.kafkaapp.controllers.SessionHelper;
import com.ntunghoi.kafkaapp.exceptions.BadRequestDataException;
import com.ntunghoi.kafkaapp.exceptions.UnknownErrorException;
import com.ntunghoi.kafkaapp.models.AccountTransaction;
import com.ntunghoi.kafkaapp.models.PagedResult;
import com.ntunghoi.kafkaapp.services.AccountTransactionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.ntunghoi.kafkaapp.configurations.OpenApiConfiguration.ACCOUNTS_TAG;

@RestController
@RequestMapping("/accounts")
@Tag(name = ACCOUNTS_TAG)
public class AccountTransactionsController {
    private static final Logger logger = LoggerFactory.getLogger(AccountTransactionsController.class);

    private final AccountTransactionsService accountTransactionsService;

    @Autowired
    public AccountTransactionsController(AccountTransactionsService accountTransactionsService) {
        this.accountTransactionsService = accountTransactionsService;
    }

    @GetMapping("/transactions")
    @Operation(summary = "List of transactions associated with the given account number")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request data",
                    content = {
                            @Content(
                                    mediaType = "application/json"
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = {
                            @Content(
                                    mediaType = "application/json"
                            )
                    }
            )
    })
    public DeferredResult<PagedResult<AccountTransaction>> getTransactionsByAccount(
            HttpSession session,
            @RequestParam(
                    name = "start_date",
                    required = true
            )
            @Parameter(
                    description = "Start of the date range",
                    example = "2000-01-01T09:00:00"
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) LocalDateTime startDate,
            @RequestParam(
                    name = "end_date",
                    required = true
            )
            @Parameter(
                    description = "End of the date range",
                    example = "2020-01-01T10:00:00"
            )
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE_TIME
            ) LocalDateTime endDate,
            @RequestParam(
                    defaultValue = "0",
                    required = false
            )
            @Parameter(
                    description = "Page number",
                    example = "0"
            ) int page,
            @RequestParam(
                    defaultValue = "20",
                    required = false
            )
            @Parameter(
                    description = "Number of transaction records per page",
                    example = "20"
            ) int size
    ) throws BadRequestDataException, UnknownErrorException {
        DeferredResult<PagedResult<AccountTransaction>>
                deferredResult = new DeferredResult<>(10000L);

        Instant now = Instant.now();
        Instant from = startDate.toInstant(ZoneOffset.UTC);
        Instant to = endDate.toInstant(ZoneOffset.UTC);

        if (from.isAfter(now)) {
            throw new BadRequestDataException("Start date should not be a future date");
        }

        if (to.isAfter(now)) {
            throw new BadRequestDataException("End date should not be a future date");
        }

        if (from.isAfter(to)) {
            throw new BadRequestDataException("Start date should not be later than the end date");
        }

        SessionHelper sessionHelper = new SessionHelper(session);
        logger.info("Debugging: {}", sessionHelper.getPreferredCurrency());
        try {
            accountTransactionsService.getAccountTransactions(
                    new AccountTransactionsService.AccountTransactionsQuery(
                            sessionHelper.getUserId(),
                            sessionHelper.getPreferredCurrency(),
                            from.toEpochMilli(),
                            to.toEpochMilli(),
                            page,
                            size
                    ),
                    result -> {
                        boolean isDone = deferredResult.setResult(result);
                        logger.info("Is result set and passed on for handling? {}", isDone);
                    }
            );
        } catch (Exception exception) {
            logger.info("Exception: {}", exception.getMessage());
            exception.printStackTrace(System.err);
            throw new UnknownErrorException(
                    "Error in retrieving transaction data",
                    exception
            );
        }

        return deferredResult;
    }
}

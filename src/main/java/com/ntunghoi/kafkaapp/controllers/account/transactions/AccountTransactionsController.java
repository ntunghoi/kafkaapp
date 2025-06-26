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

import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

import static com.ntunghoi.kafkaapp.configurations.OpenApiConfiguration.ACCOUNTS_TAG;
import static com.ntunghoi.kafkaapp.services.AccountTransactionsService.AccountTransactionsQuery;

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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No transaction found",
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
                    name = "account_number",
                    required = true
            )
            @Parameter(
                    description = "Account number",
                    example = "GB84CPBK83157663644901"
            ) String accountNNumber,
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
                    description = "Offset of the result",
                    example = "1704519941000"
            ) long offset,
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

        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedStartDate = startDate.atZone(zoneId);
        ZonedDateTime zonedEndDate = endDate.atZone(zoneId);
        ZonedDateTime zonedNow = LocalDateTime.now().atZone(zoneId);

        if (zonedStartDate.isAfter(zonedNow)) {
            throw new BadRequestDataException("Start date should not be a future date");
        }

        if (zonedEndDate.isAfter(zonedNow)) {
            throw new BadRequestDataException("End date should not be a future date");
        }

        if (zonedStartDate.isAfter(zonedEndDate)) {
            throw new BadRequestDataException("Start date should not be later than the end date");
        }

        ZonedDateTime zonedOffset = offset != 0 ?ZonedDateTime.ofInstant(Instant.ofEpochMilli(offset), zoneId) : zonedStartDate;

        if (zonedOffset.isBefore(zonedStartDate) || zonedOffset.isAfter(zonedEndDate)) {
            throw new BadRequestDataException("Offset if present should in between start date and end date parameters");
        }

        SessionHelper sessionHelper = new SessionHelper(session);
        logger.info("Debugging: {}", sessionHelper.getPreferredCurrency());
        try {
            accountTransactionsService.getAccountTransactions(
                    new AccountTransactionsQuery(
                            sessionHelper.getUserId(),
                            accountNNumber,
                            sessionHelper.getPreferredCurrency(),
                            zonedOffset.toLocalDate().atStartOfDay().atZone(zoneId).toInstant().toEpochMilli(),
                            zonedEndDate.toInstant().toEpochMilli(),
                            offset,
                            size == 0 ? 20 : size
                    ),
                    result -> {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        result.data().forEach(accountTransaction ->
                            logger.info("{} - {}", accountTransaction.id(), simpleDateFormat.format(new Date(accountTransaction.valueTimestamp())))
                        );
                        boolean isDone = deferredResult.setResult(result);
                        logger.info("Is result set and passed on for handling? {}", isDone);
                    }
            );
        } catch (Exception exception) {
            logger.error("Exception: {}\n", exception.getMessage(), exception);
            throw new UnknownErrorException(
                    "Error in retrieving transaction data",
                    exception
            );
        }

        return deferredResult;
    }
}

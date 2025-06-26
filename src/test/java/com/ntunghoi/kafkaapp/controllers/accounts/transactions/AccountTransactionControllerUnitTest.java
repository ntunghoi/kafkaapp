package com.ntunghoi.kafkaapp.controllers.accounts.transactions;

import com.ntunghoi.kafkaapp.components.AuthEntryPointJwt;
import com.ntunghoi.kafkaapp.configurations.ApplicationConfiguration;
import com.ntunghoi.kafkaapp.configurations.SecurityConfiguration;
import com.ntunghoi.kafkaapp.controllers.account.transactions.AccountTransactionsController;
import com.ntunghoi.kafkaapp.entities.UserProfileEntity;
import com.ntunghoi.kafkaapp.repositories.UserProfilesRepository;
import com.ntunghoi.kafkaapp.services.AccountTransactionsService;
import com.ntunghoi.kafkaapp.services.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AccountTransactionsController.class)
@Import({
        ApplicationConfiguration.class,
        SecurityConfiguration.class,
        AuthEntryPointJwt.class
})
public class AccountTransactionControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserProfilesRepository userProfilesRepository;

    @MockitoBean
    private AccountTransactionsService accountTransactionsService;

    @MockitoBean
    private UserProfileEntity userProfileEntity;

    private MockHttpServletRequestBuilder getRequestBuilder(String jwt) {
        return getRequestBuilder()
                .header("Authorization", String.format("Bearer %s", jwt));
    }

    private MockHttpServletRequestBuilder getRequestBuilder() {
        return get("/accounts/transactions");
    }

    @Nested
    class Unauthorized {
        @Test
        void test_GetTransactionsByAccount_MissingJwt() throws Exception {
            mockMvc.perform(getRequestBuilder())
                    .andExpect(
                            status().isUnauthorized()
                    );
        }

        @Test
        void test_GetTransactionByAccount_InvalidJwt() throws Exception {
            mockMvc.perform(getRequestBuilder("invalid_jwt"))
                    .andExpect(
                            status().isUnauthorized()
                    );
        }
    }

    @Nested
    class Authorized {
        private final String validJwt = "valid_jwt";

        @BeforeEach
        void setup () {
            String email = "testing@emai.com";
            when(userProfileEntity.getEmail()).thenReturn(email);
            when(userProfileEntity.getPreferredCurrency()).thenReturn("HKD");
            when(userProfileEntity.getId()).thenReturn(1);
            when(jwtService.extractUsername(validJwt)).thenReturn(email);
            when(jwtService.isTokenValid(validJwt, userProfileEntity)).thenReturn(true);
            when(userProfilesRepository.findByEmail(email)).thenReturn(Optional.of(userProfileEntity));
        }

        @Nested
        class AuthorizedMissingParameters {
            @Test
            void test_GetTransactionsByAccount_ValidJwt_NoParameters() throws Exception {
                mockMvc.perform(getRequestBuilder(validJwt))
                        .andExpect(
                                status().isBadRequest()
                        ).andExpect(
                                content().string("Missing value for parameter account_number")
                        );
            }

            @Test
            void test_GetTransactionsByAccount_ValidJwt_AccountNumber() throws Exception {
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        content().string("Missing value for parameter start_date")
                );

            }

            @Test
            void test_GetTransactionsByAccount_ValidJwt_MissingEndDate() throws Exception {
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                                .queryParam("start_date", "2024-01-08T00:00:00")
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        content().string("Missing value for parameter end_date")
                );
            }

            @Test
            void test_GetTransactionsByAccount_ValidJwt_MissingStartDate() throws Exception {
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                                .queryParam("end_date", "2024-01-09T00:00:00")
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        content().string("Missing value for parameter start_date")
                );
            }
        }


        @Nested
        class AuthorizedInvalidParameters {
            private final String futureDate =
                    Instant.now()
                            .plus(10, ChronoUnit.DAYS)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"));

            @Test
            void test_GetTransactionsByAccount_ValidJwt_StartDateFutureDate() throws Exception {
                System.out.println(futureDate);
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                                .queryParam("start_date", futureDate)
                                .queryParam("end_date", "2024-01-01T00:00:00")
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        jsonPath("message").value("Start date should not be a future date")
                );
            }

            @Test
            void test_GetTransactionsByAccount_ValidJwt_EndDateFutureDate() throws Exception {
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                                .queryParam("start_date", "2024-01-10T00:00:00")
                                .queryParam("end_date", futureDate)
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        jsonPath("message").value("End date should not be a future date")
                );
            }

            @Test
            void test_GetTransactionsByAccount_ValidJwt_StartDateLaterThanEndDate() throws Exception {
                mockMvc.perform(
                        getRequestBuilder(validJwt)
                                .queryParam("account_number", "DE13870962145025555422")
                                .queryParam("start_date", "2024-01-10T00:00:00")
                                .queryParam("end_date", "2024-01-01T00:00:00")
                ).andExpect(
                        status().isBadRequest()
                ).andExpect(
                        jsonPath("message").value("Start date should not be later than the end date")
                );
            }
        }
    }
}

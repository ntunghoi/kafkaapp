package com.ntunghoi.kafkaapp.comands.dataimport;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.ntunghoi.kafkaapp.components.KafkaProducer;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.ntunghoi.kafkaapp.comands.CommandParser.Command;

@Component
public class DataImportCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DataImportCommand.class);
    public final Option option = Option.builder("i")
            .longOpt("import")
            .desc("Import data")
            .hasArg(true)
            .argName("configuration file")
            .build();

    private final KafkaProducer kafkaProducer;

    @Autowired
    public DataImportCommand(KafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public String getKey() {
        return option.getLongOpt();
    }

    public record RawAccountTransaction(
            @JsonProperty("id")
            String id,

            @JsonProperty("amount_with_currency")
            String amountWithCurrency,

            @JsonProperty("value_date")
            long valueDate,

            @JsonProperty("value_timestamp")
            long valueTimestamp,

            @JsonProperty("account_number")
            String accountNumber,

            @JsonProperty("description")
            String description
    ) {
    }

    private record UserAccount(
            @JsonProperty("user_id")
            String userId,

            @JsonProperty("account_number")
            String accountNumber
    ) {
    }

    private record Config(
            String dataFilePath,
            String userAccountsTopicName,
            String accountTransactionsTopicName,
            long defaultUserAccountCreationDate
    ) {
        private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        private static Config load(String configFilePath) throws IOException, ParseException {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(new File(configFilePath));
            String dataFilePath = rootNode.get("dataFilePath").asText().trim();
            String userAccountsTopicName = rootNode.get("userAccountsTopicName").asText().trim();
            String accountTransactionsTopicName = rootNode.get("accountTransactionsTopicName").asText();
            String defaultUserAccountCreationDate = rootNode.get("defaultUserAccountCreationDate").asText();

            return new Config(
                    dataFilePath,
                    userAccountsTopicName,
                    accountTransactionsTopicName,
                    simpleDateFormat.parse(defaultUserAccountCreationDate).getTime()
            );
        }
    }

    @Override
    public void run(CommandLine commandLine) throws Exception {
        String configFilePath = commandLine.getOptionValue(option.getLongOpt()).trim();
        logger.info("Configuration file: {}", configFilePath);
        Config config = Config.load(configFilePath);
        logger.info("Load data from file: {}", config.dataFilePath);

        CsvSchema csvSchema = CsvSchema.emptySchema().withHeader();
        ObjectReader reader = new CsvMapper().readerFor(JsonNode.class).with(csvSchema);
        Map<String, Set<String>> accountNumberByUserId = new HashMap<>();
        try (MappingIterator<JsonNode> iterator = reader.readValues(new File(config.dataFilePath))) {
            int index = 0;
            while (iterator.hasNext()) {
                try {
                    JsonNode node = iterator.next();
                    String userId = node.get("user_id").asText();
                    RawAccountTransaction mapped = DataMapper.fromRaw(node);
                    kafkaProducer.sendMessage(
                            config.accountTransactionsTopicName,
                            mapped.valueTimestamp(),
                            mapped.id(),
                            mapped
                    );
                    Set<String> accountNumbers = accountNumberByUserId.get(userId);
                    String accountNumber = mapped.accountNumber();
                    boolean isAdded;
                    if (accountNumbers == null) {
                        accountNumberByUserId.put(userId, new HashSet<>(Collections.singletonList(accountNumber)));
                        isAdded = true;
                    } else {
                        isAdded = accountNumbers.add(accountNumber);
                    }
                    if (isAdded) {
                        System.out.println(String.format("%s,%s", userId, accountNumber));
                        kafkaProducer.sendMessage(
                                config.userAccountsTopicName,
                                config.defaultUserAccountCreationDate,
                                accountNumber,
                                new UserAccount(
                                        userId,
                                        accountNumber
                                )
                        );
                    }
                } catch (Exception exception) {
                    logger.error("Error in processing data in line {}: {}", index, exception.getMessage());
                }
                index++;
            }
        }
    }
}



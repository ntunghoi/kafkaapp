package com.ntunghoi.kafkaapp.comands;

import com.ntunghoi.kafkaapp.comands.dataimport.DataImportCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandParser {
    public interface Command {
        String getKey();
        void run(CommandLine commandLine) throws Exception;
    }

    private static final Logger logger = LoggerFactory.getLogger(CommandParser.class);
    private final Options options = new Options();
    private final HelpCommand helpCommand = new HelpCommand(options);
    private final List<Command> commands;

    @Autowired
    public CommandParser(DataImportCommand dataImportCommand) {
        commands = List.of(
                helpCommand,
                dataImportCommand
        );
        options.addOption(dataImportCommand.option);
    }

    public boolean process(String... args) {
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);
            for (Command command : commands) {
                if(commandLine.hasOption(command.getKey())) {
                    command.run(commandLine);
                    return true;
                }
            }
        } catch(Exception exception) {
            logger.error(exception.getMessage(), exception);
        }

        return false;
    }

    public void printHelp() {
        helpCommand.run();
    }
}

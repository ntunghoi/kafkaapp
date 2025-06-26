package com.ntunghoi.kafkaapp.comands;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class HelpCommand implements CommandParser.Command{
    private final Option option =
            Option.builder("h")
                    .longOpt("help")
                    .desc("Print the help page")
                    .hasArg(false)
                    .build();
    private final Options options;

    public HelpCommand(Options options) {
        this.options = options;
        options.addOption(option);
    }

    @Override
    public String getKey() {
        return option.getLongOpt();
    }

    @Override
    public void run(CommandLine commandLine) {
        run();
    }

    public void run() {
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("command", options);
    }
}

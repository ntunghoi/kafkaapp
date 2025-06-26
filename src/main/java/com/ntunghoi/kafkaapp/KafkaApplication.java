package com.ntunghoi.kafkaapp;

import com.ntunghoi.kafkaapp.comands.CommandParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class KafkaApplication {
    protected static boolean isDone = false;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(KafkaApplication.class, args);
        if (isDone) {
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    @Component
    @Profile("cli")
    public static class CommandLineApplication implements CommandLineRunner {
        private final CommandParser commandParser;

        @Autowired
        public CommandLineApplication(CommandParser commandParser) {
            this.commandParser = commandParser;
        }

        @Override
        public void run(String... args) throws Exception {
            if (!commandParser.process(args)) {
                commandParser.printHelp();
            }

            KafkaApplication.isDone = true;
        }
    }
}

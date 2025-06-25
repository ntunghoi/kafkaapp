package com.ntunghoi.kafkaapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class KafkaApplication {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(KafkaApplication.class, args);
    }
}

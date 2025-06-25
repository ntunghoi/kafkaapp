package com.ntunghoi.kafkaapp.configurations;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.embedded.RedisServer;

import java.io.IOException;

@Component
public class EmbeddedRedisConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisConfiguration.class);

    @Value("${spring.data.redis.port}")
    private int port;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        logger.info("Starting Redis at port {}", port);
        redisServer = new RedisServer(port);
        redisServer.start();
        logger.info("Redis started");
    }

    @PreDestroy
    public void stopRedis() throws IOException {
        if(redisServer != null) {
            logger.info("Stopping Redis");
            redisServer.stop();
            logger.info("Redis stopped");
        }
    }
}

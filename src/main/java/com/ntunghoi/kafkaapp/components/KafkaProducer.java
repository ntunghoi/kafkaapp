package com.ntunghoi.kafkaapp.components;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, Record> kafkaTemplate;

    public void sendMessage(String topic, long timestamp, String key,  Record record) {
        ProducerRecord<String, Record> producerRecord = new ProducerRecord<>(topic, null, timestamp, key, record);
        kafkaTemplate.send(producerRecord);
    }
}

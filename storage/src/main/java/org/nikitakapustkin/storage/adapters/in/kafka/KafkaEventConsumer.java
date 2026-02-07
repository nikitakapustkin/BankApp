package org.nikitakapustkin.storage.adapters.in.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.nikitakapustkin.storage.application.EventIngestionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEventConsumer {
    private final EventIngestionService eventIngestionService;

    @KafkaListener(topics = "${kafka.topics.user}", groupId = "${kafka.consumer.group-id}")
    public void consumeUserEvents(ConsumerRecord<String, String> record) {
        eventIngestionService.consumeUserEvents(record.value());
    }

    @KafkaListener(topics = "${kafka.topics.account}", groupId = "${kafka.consumer.group-id}")
    public void consumeAccountEvents(ConsumerRecord<String, String> record) {
        eventIngestionService.consumeAccountEvents(record.value());
    }

    @KafkaListener(topics = "${kafka.topics.transaction}", groupId = "${kafka.consumer.group-id}")
    public void consumeTransactionEvents(ConsumerRecord<String, String> record) {
        eventIngestionService.consumeTransactionEvents(record.value());
    }
}

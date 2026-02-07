package org.nikitakapustkin.storage.adapters.in.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Value("${kafka.topics.account}")
    private String accountTopicName;

    @Value("${kafka.topics.user}")
    private String userTopicName;

    @Value("${kafka.topics.transaction}")
    private String transactionTopicName;

    @Value("${kafka.consumer.dlt-suffix:.dlt}")
    private String dltSuffix;

    @Bean
    public NewTopic accountTopic() {
        return TopicBuilder.name(accountTopicName).build();
    }

    @Bean
    public NewTopic userTopic() {
        return TopicBuilder.name(userTopicName).build();
    }

    @Bean
    public NewTopic transactionTopic() {
        return TopicBuilder.name(transactionTopicName).build();
    }

    @Bean
    public NewTopic accountDltTopic() {
        return TopicBuilder.name(accountTopicName + dltSuffix).build();
    }

    @Bean
    public NewTopic userDltTopic() {
        return TopicBuilder.name(userTopicName + dltSuffix).build();
    }

    @Bean
    public NewTopic transactionDltTopic() {
        return TopicBuilder.name(transactionTopicName + dltSuffix).build();
    }
}

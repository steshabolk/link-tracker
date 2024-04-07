package edu.java.service.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.dto.response.LinkUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class QueueUpdateSender implements UpdateSender {

    @Value("${app.kafka-topics.link-update.name}")
    private String topic;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public boolean send(LinkUpdate update) {
        try {
            kafkaTemplate.send(topic, mapper.writeValueAsString(update));
            return true;
        } catch (RuntimeException | JsonProcessingException ex) {
            log.warn("error when sending an update to kafka: topic={}\n{}: {}", topic, ex.getMessage(), ex.getCause());
            return false;
        }
    }
}

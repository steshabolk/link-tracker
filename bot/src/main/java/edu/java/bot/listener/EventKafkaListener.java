package edu.java.bot.listener;

import edu.java.bot.dto.request.LinkUpdate;
import edu.java.bot.service.BotService;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@Validated
@Component
@ConditionalOnProperty(prefix = "app", name = "use-queue", havingValue = "true")
public class EventKafkaListener {

    private final BotService botService;

    @RetryableTopic(attempts = "${app.kafka-retry.max-attempts}",
                    backoff = @Backoff(delayExpression = "${app.kafka-retry.backoff}",
                                       multiplierExpression = "${app.kafka-retry.multiplier}",
                                       maxDelayExpression = "${app.kafka-retry.max-backoff}"),
                    topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
                    dltTopicSuffix = "-dlq",
                    dltStrategy = DltStrategy.FAIL_ON_ERROR,
                    kafkaTemplate = "kafkaTemplate",
                    exclude = {ValidationException.class})
    @KafkaListener(topics = "${app.kafka-topics.link-update.name}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(@Payload @Valid LinkUpdate linkUpdate) {
        botService.sendLinkUpdate(linkUpdate);
    }
}

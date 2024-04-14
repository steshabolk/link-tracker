package edu.java.bot.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.configuration.KafkaConfig;
import edu.java.bot.dto.request.LinkUpdate;
import edu.java.bot.service.BotService;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.verify;

@TestPropertySource(
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka-retry.max-attempts=2", "app.kafka-retry.backoff=1000", "app.kafka-retry.multiplier=1"})
@SpringBootTest(classes = {EventKafkaListener.class})
@ContextConfiguration(classes = {KafkaConfig.class, KafkaAutoConfiguration.class})
@Import(value = {ValidationAutoConfiguration.class})
@EmbeddedKafka(topics = {"link-update", "link-update-retry-0", "link-update-dlq"}, partitions = 1)
class EventKafkaListenerTest {

    @MockBean
    private BotService botService;
    @MockBean
    private ApplicationConfig applicationConfig;
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    @Qualifier("kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;
    private ObjectMapper mapper = new ObjectMapper();
    private Consumer<String, String> mainConsumer;
    private Consumer<String, String> retryConsumer;
    private Consumer<String, String> dltConsumer;
    private static final String TOPIC = "link-update";

    @BeforeEach
    void init() {
        mainConsumer = consumer(TOPIC);
        retryConsumer = consumer(TOPIC + "-retry-0");
        dltConsumer = consumer(TOPIC + "-dlq");
    }

    @AfterEach
    void shutdown() {
        mainConsumer.close();
        retryConsumer.close();
        dltConsumer.close();
    }

    @Nested
    class ListenLinkUpdateTest {

        @SneakyThrows
        @Test
        void whenMainTopicSucceeds_thenNoRetryAndDlt() {
            LinkUpdate linkUpdate = new LinkUpdate(
                1L,
                URI.create("https://github.com/JetBrains/kotlin"),
                "new update",
                List.of(2L)
            );
            String expectedMessage = mapper.writeValueAsString(linkUpdate);

            kafkaTemplate.send(TOPIC, linkUpdate);

            ConsumerRecords<String, String> mainRecords =
                KafkaTestUtils.getRecords(mainConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> retryRecords =
                KafkaTestUtils.getRecords(retryConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> dltRecords =
                KafkaTestUtils.getRecords(dltConsumer, Duration.ofSeconds(5));

            assertThat(mainRecords.count()).isEqualTo(1);
            assertThat(mainRecords.iterator().next().value()).isEqualTo(expectedMessage);
            assertThat(retryRecords.count()).isEqualTo(0);
            assertThat(dltRecords.count()).isEqualTo(0);

            verify(botService, after(5000).times(1)).sendLinkUpdate(any(LinkUpdate.class));
        }

        @ParameterizedTest
        @MethodSource("edu.java.bot.listener.EventKafkaListenerTest#invalidLinkUpdate")
        @SneakyThrows
        void whenMainTopicFailsDueToValidationException_thenNoRetry_thenDlt(LinkUpdate linkUpdate) {
            String expectedMessage = mapper.writeValueAsString(linkUpdate);

            kafkaTemplate.send(TOPIC, linkUpdate);

            ConsumerRecords<String, String> mainRecords =
                KafkaTestUtils.getRecords(mainConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> retryRecords =
                KafkaTestUtils.getRecords(retryConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> dltRecords =
                KafkaTestUtils.getRecords(dltConsumer, Duration.ofSeconds(5));

            assertThat(mainRecords.count()).isEqualTo(1);
            assertThat(mainRecords.iterator().next().value()).isEqualTo(expectedMessage);
            assertThat(retryRecords.count()).isEqualTo(0);
            assertThat(dltRecords.count()).isEqualTo(1);
            assertThat(dltRecords.iterator().next().value()).isEqualTo(expectedMessage);

            verify(botService, after(5000).never()).sendLinkUpdate(any(LinkUpdate.class));
        }

        @SneakyThrows
        @Test
        void whenMainTopicFailsDueToDeserializationException_thenNoRetry_thenDlt() {
            String message = "dummy";

            kafkaTemplate.send(TOPIC, message.getBytes());

            ConsumerRecords<String, String> mainRecords =
                KafkaTestUtils.getRecords(mainConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> retryRecords =
                KafkaTestUtils.getRecords(retryConsumer, Duration.ofSeconds(5));
            ConsumerRecords<String, String> dltRecords =
                KafkaTestUtils.getRecords(dltConsumer, Duration.ofSeconds(5));

            assertThat(mainRecords.count()).isEqualTo(1);
            assertThat(mainRecords.iterator().next().value()).isEqualTo(message);
            assertThat(retryRecords.count()).isEqualTo(0);
            assertThat(dltRecords.count()).isEqualTo(1);
            assertThat(dltRecords.iterator().next().value()).isEqualTo(message);

            verify(botService, after(5000).never()).sendLinkUpdate(any(LinkUpdate.class));
        }
    }

    private static Stream<Arguments> invalidLinkUpdate() {
        return Stream.of(
            Arguments.of(new LinkUpdate(
                null,
                URI.create("https://github.com/JetBrains/kotlin"),
                "new update",
                List.of(2L)
            )),
            Arguments.of(new LinkUpdate(1L, null, "new update", List.of(2L))),
            Arguments.of(new LinkUpdate(1L, URI.create("https://github.com/JetBrains/kotlin"), null, List.of(2L))),
            Arguments.of(new LinkUpdate(1L, URI.create("https://github.com/JetBrains/kotlin"), "", List.of(2L))),
            Arguments.of(new LinkUpdate(1L, URI.create("https://github.com/JetBrains/kotlin"), "", null)),
            Arguments.of(new LinkUpdate(1L, URI.create("https://github.com/JetBrains/kotlin"), "", List.of()))
        );
    }

    private Consumer<String, String> consumer(String topic) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, String> consumer = consumerFactory.createConsumer();
        consumer.subscribe(List.of(topic));
        return consumer;
    }
}

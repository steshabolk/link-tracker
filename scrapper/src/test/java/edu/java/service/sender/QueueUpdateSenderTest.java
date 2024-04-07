package edu.java.service.sender;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.configuration.ApplicationConfig;
import edu.java.configuration.KafkaConfig;
import edu.java.dto.response.LinkUpdate;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@SpringBootTest(classes = {QueueUpdateSender.class, ObjectMapper.class})
@ContextConfiguration(classes = {KafkaConfig.class, KafkaAutoConfiguration.class})
@EmbeddedKafka(topics = {"link-update"}, partitions = 1)
class QueueUpdateSenderTest {

    @MockBean
    private ApplicationConfig applicationConfig;
    @Autowired
    private QueueUpdateSender updateSender;
    @Autowired
    private KafkaProperties kafkaProperties;
    @Autowired
    private ObjectMapper mapper;
    private Consumer<String, String> consumer;
    private static final String TOPIC = "link-update";
    private static final LinkUpdate LINK_UPDATE = new LinkUpdate(
        1L,
        URI.create("https://github.com/JetBrains/kotlin"),
        "new update",
        List.of(2L)
    );

    @BeforeEach
    void init() {
        consumer = consumer(TOPIC);
    }

    @AfterEach
    void shutdown() {
        consumer.close();
    }

    @Nested
    class SendLinkUpdateTest {

        @SneakyThrows
        @Test
        void shouldReturnTrueWhenSucceeds() {
            String expectedMessage = mapper.writeValueAsString(LINK_UPDATE);

            boolean isSent = updateSender.send(LINK_UPDATE);

            assertThat(isSent).isTrue();

            ConsumerRecords<String, String> records =
                KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(15), 1);

            assertThat(records.count()).isEqualTo(1);
            assertThat(records.iterator().next().value()).isEqualTo(expectedMessage);
        }
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

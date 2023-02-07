package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * reference from
 * https://www.geekyhacker.com/2020/10/03/test-spring-kafka-consumer-and-producer-with-embeddedkafka/
 */
@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@ActiveProfiles("test")
class LibraryEventProducerIntegrationTest {

    private static String TOPIC = "library-events";

    private BlockingQueue<ConsumerRecord<String, String>> records;

    private KafkaMessageListenerContainer<String, String> container;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private LibraryEventProducer producer;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1","true",embeddedKafkaBroker));
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(configs,new StringDeserializer(),new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
    }


    @AfterAll
    void tearDown() {
        container.stop();
    }

    @Test
    void testWriteToKafka() throws InterruptedException, JsonProcessingException {
        // Create a user and write to Kafka
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();
        producer.sendLibraryEventApproch2(libraryEvent);

        // Read the message (John Wick user) with a test consumer from Kafka and assert its properties
        ConsumerRecord<String, String> message = records.poll(500, TimeUnit.MILLISECONDS);
        assertNotNull(message);

        LibraryEvent result = objectMapper.readValue(message.value(), LibraryEvent.class);
        assertNotNull(result);
        assertEquals("Dilip", result.getBook().getBookAuthor());

    }
}

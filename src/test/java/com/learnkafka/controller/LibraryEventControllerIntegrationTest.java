package com.learnkafka.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.LibraryEventsProducerApplication;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = LibraryEventsProducerApplication.class , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap. servers=${spring.embedded.kafka.brokers}"
})
public class LibraryEventControllerIntegrationTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1","true",embeddedKafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs,new StringDeserializer(),new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @Timeout(10)
    void postLibraryEvent() throws JsonProcessingException, InterruptedException {
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

        HttpHeaders headers = new HttpHeaders();
        headers.set("cotent-type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent,headers);
        ResponseEntity<LibraryEvent> responseEntity = testRestTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEvent.class);
        assertEquals(HttpStatus.CREATED,responseEntity.getStatusCode());
        ConsumerRecord<String, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        String expected = new ObjectMapper().writeValueAsString(libraryEvent);
        String value = consumerRecord.value();
        assertEquals(expected,value);

    }
    @Test
    @Timeout(10)
    void putLibraryEvent() throws JsonProcessingException, InterruptedException {
        Book book = Book.builder()
                .bookId(123)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId("55")
                .libraryEventType(LibraryEventType.UPDATE)
                .book(book)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set("cotent-type", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent,headers);
        ResponseEntity<LibraryEvent> responseEntity = testRestTemplate.exchange("/v1/libraryevent", HttpMethod.PUT, request, LibraryEvent.class);
        assertEquals(HttpStatus.OK,responseEntity.getStatusCode());
        ConsumerRecord<String, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events");
        String expected = new ObjectMapper().writeValueAsString(libraryEvent);
        String value = consumerRecord.value();
        assertEquals(expected,value);

    }
}

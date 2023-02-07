package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
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
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap. servers=${spring.embedded.kafka.brokers}"
})
public class LibraryEventProducerIntegrationTest1 {

    private static String TOPIC = "library-events";
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private LibraryEventProducer libraryEventProducer;

    @Autowired
    private ObjectMapper objectMapper;

    BlockingQueue<ConsumerRecord<String, String>> records;

    KafkaMessageListenerContainer<String, String> container;




    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1","true",embeddedKafkaBroker));
        //configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), new StringDeserializer());
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        //embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        container.stop();
    }

    @Test
    //@Timeout(10)
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

//        HttpHeaders headers = new HttpHeaders();
//        headers.set("cotent-type", MediaType.APPLICATION_JSON_VALUE);
//        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent,headers);
//        ResponseEntity<LibraryEvent> responseEntity = testRestTemplate.exchange("/v1/libraryevent", HttpMethod.POST, request, LibraryEvent.class);
//        assertEquals(HttpStatus.CREATED,responseEntity.getStatusCode());
        libraryEventProducer.sendLibraryEventApproch2(libraryEvent);
        //Thread.sleep(10000);
        ConsumerRecord<String,String> singleRecord = records.poll(500, TimeUnit.MILLISECONDS);
        String expected = new ObjectMapper().writeValueAsString(libraryEvent);
        String value = singleRecord.value();
        assertEquals(expected,value);

    }
}

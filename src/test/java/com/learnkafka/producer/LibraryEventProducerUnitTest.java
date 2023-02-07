package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableAnyProperty;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.SettableListenableFuture;
import scala.Int;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {
    @InjectMocks
    LibraryEventProducer libraryEventProducer;
    @Mock
    KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper;
    @Test
    void sendLibraryEvent_Approch2_failur() throws JsonProcessingException, ExecutionException, InterruptedException {
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
//        SettableListenableFuture settableListenableFuture = new SettableListenableFuture();
//        settableListenableFuture.setException();
        CompletableFuture completableFuture = new CompletableFuture();
        completableFuture.completeExceptionally(new RuntimeException("Exception Calling Kafka"));
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(completableFuture);
        assertThrows(Exception.class, ()->libraryEventProducer.sendLibraryEventApproch2(libraryEvent).get());
    }
    @Test
    void sendLibraryEvent_Approch2_success() throws JsonProcessingException, ExecutionException, InterruptedException {
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
//        SettableListenableFuture settableListenableFuture = new SettableListenableFuture();
//        settableListenableFuture.set();
//        settableListenableFuture.setException();
        String record = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord<String, String>  producerRecord = new ProducerRecord<>("library-events",libraryEvent.getLibraryEventId(),record);

        RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events",1),1l,1,System.currentTimeMillis(),1,2);
        SendResult<String, String> sendResult = new SendResult<String,String>(producerRecord,recordMetadata);

        CompletableFuture<SendResult<String,String>> completableFuture = new CompletableFuture<>();
        completableFuture.complete(sendResult);

        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(completableFuture);
        String expected = "Message Sent Successfully for Key : null and value is {\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":123,\"bookName\":\"Kafka using Spring boot\",\"bookAuthor\":\"Dilip\"}} , partition is 1";
        CompletableFuture<SendResult<String, String>> sendResultCompletableFuture = libraryEventProducer.sendLibraryEventApproch2(libraryEvent);
        SendResult<String, String> sendResult1 = sendResultCompletableFuture.get();
        assert  sendResult1.getRecordMetadata().partition()==1;

    }
}

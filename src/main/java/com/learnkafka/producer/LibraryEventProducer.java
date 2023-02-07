package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    private String topic = "library-events";

    /**
     * This method send message to kafka topic, but it shows asynchronous behaviour.
     * @param libraryEvent
     * @throws JsonProcessingException
     */
    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        String key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        CompletableFuture<SendResult<String, String>> sendResultCompletableFuture = kafkaTemplate.sendDefault(key, value);
        sendResultCompletableFuture.whenComplete((result,ex)->{
            if(ex==null){
                onSuccess(key, value,result);
            }else{
                onFailure(key, value,ex);
            }
        });
    }

    public CompletableFuture<SendResult<String, String>> sendLibraryEventApproch2(LibraryEvent libraryEvent) throws JsonProcessingException {
        String key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        ProducerRecord<String, String> producerRecord = buildProducerRecord(key, value, topic);
        CompletableFuture<SendResult<String, String>> sendResultCompletableFuture = kafkaTemplate.send(producerRecord);
        sendResultCompletableFuture.whenComplete((result,ex)->{
            if(ex==null){
                onSuccess(key, value,result);
            }else{
                onFailure(key, value,ex);
            }
        });
        return sendResultCompletableFuture;
    }

    private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic) {
        List<Header> recordHeader = List.of(new RecordHeader("envet-source", "scanner".getBytes()));//we can add multiple hearder just put, and add
        return  new ProducerRecord<>(topic, null, key, value, recordHeader);
    }

    public SendResult<String, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        String key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<String, String> integerStringSendResult = null;
        try {
            integerStringSendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);


            //integerStringSendResult = kafkaTemplate.sendDefault(key, value).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("ExecutionException/InterruptedException Error Sending the message and the exception is {}", e.getMessage());
            throw e;
        } catch (Exception e){
            log.error("Exception Error Sending the message and the exception is {}", e.getMessage());
            throw e;
        }
        return integerStringSendResult;
    }
    private void onFailure(String key, String value, Throwable ex) {
        log.error("Error Sending the Message and the exception is {}", ex.getMessage());
        try{
            throw ex;
        }catch (Throwable throwable){
            log.error("Error in OnRailure: {}",throwable.getMessage());
        }
    }

    private void onSuccess(String key, String value, SendResult<String, String> result) {
        log.info("Message Sent Successfully for Key : {} and value is {} , partition is {}", key, value, result.getRecordMetadata().partition());
    }
}

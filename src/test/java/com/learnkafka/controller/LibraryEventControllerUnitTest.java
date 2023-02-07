package com.learnkafka.controller;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.learnkafka.LibraryEventsProducerApplication;
//import com.learnkafka.controller.LibraryEventController;
//import com.learnkafka.domain.Book;
//import com.learnkafka.domain.LibraryEvent;
//import com.learnkafka.domain.LibraryEventType;
//import com.learnkafka.producer.LibraryEventProducer;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
////import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
//import static org.mockito.Mockito.doNothing;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.controller.LibraryEventController;
import com.learnkafka.domain.Book;
import com.learnkafka.domain.LibraryEvent;
import com.learnkafka.domain.LibraryEventType;
import com.learnkafka.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)


public class LibraryEventControllerUnitTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    private LibraryEventProducer libraryEventProducer;
    @Autowired
    ObjectMapper objectMapper;
    @Test
    void postLibraryEvent() throws Exception {
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
        String request = objectMapper.writeValueAsString(libraryEvent);
        //doNothing().when(libraryEventProducer).sendLibraryEventApproch2(libraryEvent);
        when(libraryEventProducer.sendLibraryEventApproch2(isA(LibraryEvent.class))).thenReturn(null);
//        mockMvc.perform(post("/v1/libraryevent")
//                .content(request)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isCreated());

//        mockMvc.perform(MockMvcRequestBuilders
//                .post("/v1/libraryevent")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isCreated());

        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(libraryEvent));
        mockMvc.perform(mockRequest)
                .andExpect(status().isCreated());
    }

    @Test
    void postLibraryEvent_4XX() throws Exception {
        Book book = Book.builder()
                .bookId(null)
                .bookAuthor(null)
                .bookName("Kafka using Spring boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.NEW)
                .book(book)
                .build();
        String request = objectMapper.writeValueAsString(libraryEvent);
        //doNothing().when(libraryEventProducer).sendLibraryEventApproch2(libraryEvent);
        when(libraryEventProducer.sendLibraryEventApproch2(isA(LibraryEvent.class))).thenReturn(null);
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.post("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(libraryEvent));
        String expectedErrorMessage = "book.bookAuthor-must not be blank, book.bookId-must not be null";
        mockMvc.perform(mockRequest)
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
    @Test
    void putLibraryEvent() throws Exception {
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
        String request = objectMapper.writeValueAsString(libraryEvent);
        //doNothing().when(libraryEventProducer).sendLibraryEventApproch2(libraryEvent);
        when(libraryEventProducer.sendLibraryEventApproch2(isA(LibraryEvent.class))).thenReturn(null);
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(libraryEvent));
        mockMvc.perform(mockRequest)
                .andExpect(status().isOk());
    }

    @Test
    void putLibraryEvent_4XX() throws Exception {
        Book book = Book.builder()
                .bookId(121)
                .bookAuthor("Dilip")
                .bookName("Kafka using Spring boot")
                .build();
        LibraryEvent libraryEvent = LibraryEvent.builder()
                .libraryEventId(null)
                .libraryEventType(LibraryEventType.UPDATE)
                .book(book)
                .build();
        String request = objectMapper.writeValueAsString(libraryEvent);
        //doNothing().when(libraryEventProducer).sendLibraryEventApproch2(libraryEvent);
        when(libraryEventProducer.sendLibraryEventApproch2(isA(LibraryEvent.class))).thenReturn(null);
        MockHttpServletRequestBuilder mockRequest = MockMvcRequestBuilders.put("/v1/libraryevent")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(libraryEvent));
        String expectedErrorMessage = "Please pass the LibraryEventId";
        mockMvc.perform(mockRequest)
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));
    }
}

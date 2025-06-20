package com.example.legaldocsimplifier.services.impl;

import com.example.legaldocsimplifier.services.OpenAIClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenAIClientServiceImplTest {
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private OpenAIClientService openAIClientService;

    // Use a dummy API key for testing
    private final String dummyApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        // Use reflection or a constructor if available to inject the API key
        openAIClientService = new OpenAIClientServiceImpl(restTemplate, objectMapper);
        // If using @Value injection, set the field via reflection for testing
        try {
            var field = OpenAIClientServiceImpl.class.getDeclaredField("openAIApiKey");
            field.setAccessible(true);
            field.set(openAIClientService, dummyApiKey);
        } catch (Exception ignored) {}
    }

    @Test
    void callOpenAI_shouldReturnExtractedContent_whenResponseIsValid() throws Exception {
        String documentText = "Test legal document";
        String prompt = "Please simplify and summarize the following legal document:\n\n" + documentText;

        // Mock OpenAI API response JSON
        String responseBody = "{\"choices\":[{\"message\":{\"content\":\"Simplified summary.\"}}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq("https://api.openai.com/v1/chat/completions"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        // Mock ObjectMapper parsing
        JsonNode mockRoot = mock(JsonNode.class);
        JsonNode mockChoices = mock(JsonNode.class);
        JsonNode mockMessage = mock(JsonNode.class);

        when(objectMapper.readTree(responseBody)).thenReturn(mockRoot);
        when(mockRoot.path("choices")).thenReturn(mockChoices);
        when(mockChoices.isArray()).thenReturn(true);
        when(mockChoices.size()).thenReturn(1);
        when(mockChoices.get(0)).thenReturn(mockMessage);
        when(mockMessage.path("message")).thenReturn(mockMessage);
        when(mockMessage.path("content")).thenReturn(mockMessage);
        when(mockMessage.asText()).thenReturn("Simplified summary.");

        String result = openAIClientService.callOpenAI(documentText);

        assertEquals("Simplified summary.", result);

        // Verify correct API call
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq("https://api.openai.com/v1/chat/completions"),
                captor.capture(),
                eq(String.class)
        );
        HttpEntity<Map<String, Object>> sentRequest = captor.getValue();
        assertNotNull(sentRequest);
        assertTrue(sentRequest.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON));
        assertEquals("Bearer " + dummyApiKey, sentRequest.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        Map<String, Object> body = sentRequest.getBody();
        assertEquals("gpt-4", body.get("model"));
        assertEquals(0.2, body.get("temperature"));
        List<?> messages = (List<?>) body.get("messages");
        assertEquals(1, messages.size());
        Map<?, ?> message = (Map<?, ?>) messages.get(0);
        assertEquals("user", message.get("role"));
        assertEquals(prompt, message.get("content"));
    }

    @Test
    void callOpenAI_shouldReturnErrorMessage_whenResponseCannotBeParsed() throws Exception {
        String documentText = "Test legal document";
        String invalidResponse = "invalid json";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(invalidResponse, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        when(objectMapper.readTree(invalidResponse)).thenThrow(new RuntimeException("Parsing error"));

        String result = openAIClientService.callOpenAI(documentText);

        assertEquals("Could not extract summary from OpenAI response.", result);
    }

    @Test
    void callOpenAI_shouldHandleRestTemplateException() {
        String documentText = "Test legal document";

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("API error"));

        // Depending on your implementation, you may want to catch and handle this exception in your service.
        // Here, we expect the exception to propagate.
        assertThrows(RuntimeException.class, () -> openAIClientService.callOpenAI(documentText));
    }
}
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
    private OpenAIClientServiceImpl openAIClientService;

    // Use a dummy API key for testing
    private final String dummyApiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        objectMapper = mock(ObjectMapper.class);
        openAIClientService = new OpenAIClientServiceImpl(restTemplate, objectMapper);
        try {
            var field = OpenAIClientServiceImpl.class.getDeclaredField("openAIApiKey");
            field.setAccessible(true);
            field.set(openAIClientService, dummyApiKey);
        } catch (Exception ignored) {}
    }

    @Test
    void simplifyWithPrompt_shouldReturnExtractedContent_whenResponseIsValid() throws Exception {
        String prompt = "Test prompt";
        String responseBody = "{\"choices\":[{\"message\":{\"content\":\"Simplified result.\"}}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(
                eq("https://api.openai.com/v1/chat/completions"),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

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
        when(mockMessage.asText()).thenReturn("Simplified result.");

        String result = openAIClientService.simplifyWithPrompt(prompt);
        assertEquals("Simplified result.", result);
    }

    @Test
    void simplifyDocumentWithChunking_shouldReturnSingleSummary_whenTextFitsInOneChunk() throws Exception {
        String doc = "Short legal document.";
        int chunkSize = 100;
        String expectedPrompt = "You are an AI assistant.\nSimplify:\n" + doc;
        String responseBody = "{\"choices\":[{\"message\":{\"content\":\"Short summary.\"}}]}";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

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
        when(mockMessage.asText()).thenReturn("Short summary.");

        String result = openAIClientService.simplifyDocumentWithChunking(doc, chunkSize);
        assertEquals("Short summary.", result);
    }

    @Test
    void simplifyDocumentWithChunking_shouldReturnMergedSummary_whenTextRequiresChunking() throws Exception {
        String chunk1 = "First part of a long document.";
        String chunk2 = "Second part of a long document.";
        String doc = chunk1 + " " + chunk2;
        int chunkSize = 5; // Force two chunks

        // Mock responses for each chunk and the merge
        String summary1 = "Summary of first chunk.";
        String summary2 = "Summary of second chunk.";
        String merged = "Final merged summary.";

        String responseBody1 = "{\"choices\":[{\"message\":{\"content\":\"" + summary1 + "\"}}]}";
        String responseBody2 = "{\"choices\":[{\"message\":{\"content\":\"" + summary2 + "\"}}]}";
        String responseBodyMerged = "{\"choices\":[{\"message\":{\"content\":\"" + merged + "\"}}]}";

        ResponseEntity<String> responseEntity1 = new ResponseEntity<>(responseBody1, HttpStatus.OK);
        ResponseEntity<String> responseEntity2 = new ResponseEntity<>(responseBody2, HttpStatus.OK);
        ResponseEntity<String> responseEntityMerged = new ResponseEntity<>(responseBodyMerged, HttpStatus.OK);

        // Set up the sequence of responses for each API call
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity1)
                .thenReturn(responseEntity2)
                .thenReturn(responseEntityMerged);

        // Mock ObjectMapper for each response
        JsonNode mockRoot1 = mock(JsonNode.class);
        JsonNode mockChoices1 = mock(JsonNode.class);
        JsonNode mockMessage1 = mock(JsonNode.class);
        when(objectMapper.readTree(responseBody1)).thenReturn(mockRoot1);
        when(mockRoot1.path("choices")).thenReturn(mockChoices1);
        when(mockChoices1.isArray()).thenReturn(true);
        when(mockChoices1.size()).thenReturn(1);
        when(mockChoices1.get(0)).thenReturn(mockMessage1);
        when(mockMessage1.path("message")).thenReturn(mockMessage1);
        when(mockMessage1.path("content")).thenReturn(mockMessage1);
        when(mockMessage1.asText()).thenReturn(summary1);

        JsonNode mockRoot2 = mock(JsonNode.class);
        JsonNode mockChoices2 = mock(JsonNode.class);
        JsonNode mockMessage2 = mock(JsonNode.class);
        when(objectMapper.readTree(responseBody2)).thenReturn(mockRoot2);
        when(mockRoot2.path("choices")).thenReturn(mockChoices2);
        when(mockChoices2.isArray()).thenReturn(true);
        when(mockChoices2.size()).thenReturn(1);
        when(mockChoices2.get(0)).thenReturn(mockMessage2);
        when(mockMessage2.path("message")).thenReturn(mockMessage2);
        when(mockMessage2.path("content")).thenReturn(mockMessage2);
        when(mockMessage2.asText()).thenReturn(summary2);

        JsonNode mockRootMerged = mock(JsonNode.class);
        JsonNode mockChoicesMerged = mock(JsonNode.class);
        JsonNode mockMessageMerged = mock(JsonNode.class);
        when(objectMapper.readTree(responseBodyMerged)).thenReturn(mockRootMerged);
        when(mockRootMerged.path("choices")).thenReturn(mockChoicesMerged);
        when(mockChoicesMerged.isArray()).thenReturn(true);
        when(mockChoicesMerged.size()).thenReturn(1);
        when(mockChoicesMerged.get(0)).thenReturn(mockMessageMerged);
        when(mockMessageMerged.path("message")).thenReturn(mockMessageMerged);
        when(mockMessageMerged.path("content")).thenReturn(mockMessageMerged);
        when(mockMessageMerged.asText()).thenReturn(merged);

        String result = openAIClientService.simplifyDocumentWithChunking(doc, chunkSize);
        assertEquals(merged, result);
    }
}

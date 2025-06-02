package com.example.legaldocsimplifier.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIClientService {
    private static final String OPENAI_COM_V_1_CHAT_COMPLETIONS = "https://api.openai.com/v1/chat/completions";
    private static final String LEGAL_DOCUMENT_PROMPT = "Please simplify and summarize the following legal document:\n\n";
    private static final String MODEL = "model";
    private static final String GPT_4 = "gpt-4";
    private static final String MESSAGES = "messages";
    private static final String TEMPERATURE = "temperature";
    private static final String ROLE = "role";
    private static final String USER = "user";
    private static final String CONTENT = "content";
    private static final String CHOICES = "choices";
    private static final String MESSAGE = "message";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAIApiKey;

    public OpenAIClientService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String callOpenAI(String documentText) {
        String prompt = LEGAL_DOCUMENT_PROMPT + documentText;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIApiKey);

        Map<String, Object> message = Map.of(ROLE, USER, CONTENT, prompt);

        Map<String, Object> requestBody = Map.of(
                MODEL, GPT_4,
                MESSAGES, List.of(message),
                TEMPERATURE, 0.2
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                OPENAI_COM_V_1_CHAT_COMPLETIONS,
                request,
                String.class
        );

        return extractContentFromResponse(response.getBody());
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.path(CHOICES);
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path(MESSAGE);
                return message.path(CONTENT).asText();
            }
        } catch (Exception e) {
            // Log error or handle as needed
        }
        return "Could not extract summary from OpenAI response.";
    }
}
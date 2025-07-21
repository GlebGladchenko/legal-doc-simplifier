package org.novalegal.services.impl;

import org.novalegal.services.OpenAIClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIClientServiceImpl implements OpenAIClientService {
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
    private static final String GPT_3_5_TURBO_16_K = "gpt-3.5-turbo-16k";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAIApiKey;

    public OpenAIClientServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // Main chunking-based simplification method
    public String simplifyDocumentWithChunking(String doc, int chunkSize) {
        List<String> chunkedText = chunkText(doc, chunkSize);
        if (chunkedText.size() == 1) {
            // Only one chunk, no need to merge
            String prompt = "You are a legal assistant. Simplify and summarize the following legal document in plain English.\n" +
                    "At the end, include a section titled '⚠️ Potential Concerns' if any unusual or important details appear.\n\n" +
                    chunkedText.get(0);
            return simplifyWithPrompt(prompt);
        }

        List<String> summaries = new ArrayList<>();
        String previousSummary = "";

        for (int i = 0; i < chunkedText.size(); i++) {
            String chunk = chunkedText.get(i);
            String prompt;

            if (i == 0) {
                prompt = "You are a legal assistant. Simplify and summarize the following legal section in plain English.\n" +
                        "Include a section titled '⚠️ Potential Concerns' if needed.\n\n" +
                        chunk;
            } else {
                prompt = "You are a legal assistant.\n" +
                        "Summary of the previous section:\n" + previousSummary + "\n\n" +
                        "Now simplify and summarize this new section in plain English. " +
                        "Also include a '⚠️ Potential Concerns' section if applicable.\n\n" +
                        chunk;
            }

            String summary = simplifyWithPrompt(prompt);
            summaries.add(summary);
            previousSummary = summary;
        }

        String mergedPrompt = "You are a legal assistant. Combine the following section summaries into a full plain-English explanation of the document.\n\n" +
                "Include a final section at the end titled '⚠️ Potential Concerns' combining all important issues or red flags from the summaries.\n\n" +
                String.join("\n\n", summaries);

        return simplifyWithPrompt(mergedPrompt);
    }

    public String generateMeetingSummaryFromSegmentsChunked(String whisperJson) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root;
        try {
            root = mapper.readTree(whisperJson);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse Whisper response", e);
        }

        JsonNode segments = root.get("segments");
        if (segments == null || !segments.isArray()) {
            throw new IllegalArgumentException("Missing 'segments' array");
        }

        StringBuilder allText = new StringBuilder();
        List<String> entries = new ArrayList<>();

        for (JsonNode segment : segments) {
            double start = segment.get("start").asDouble();
            double end = segment.get("end").asDouble();
            String text = segment.get("text").asText();
            String timestamp = String.format("[%02d:%02d - %02d:%02d]",
                    (int)(start / 60), (int)(start % 60),
                    (int)(end / 60), (int)(end % 60));

            String entry = timestamp + " " + text;
            entries.add(entry);
            allText.append(entry).append("\n");
        }

        // If entire text fits comfortably under 13,000 characters, no need to chunk
        if (allText.length() <= 13000) {
            String prompt = buildChunkPrompt(allText.toString(), 1);
            String rawSummary = simplifyWithPrompt(prompt);
            return buildFinalSummary(List.of(rawSummary));
        }

        // Otherwise chunk the entries
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        int chunkCharLimit = 12000;

        for (String entry : entries) {
            if (currentChunk.length() + entry.length() + 1 > chunkCharLimit) {
                chunks.add(currentChunk.toString());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(entry).append("\n");
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        // Summarize each chunk
        List<String> partialSummaries = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            String prompt = buildChunkPrompt(chunks.get(i), i + 1);
            String summary = simplifyWithPrompt(prompt);
            partialSummaries.add(summary);
        }

        return buildFinalSummary(partialSummaries);
    }

    private String buildChunkPrompt(String chunkText, int partNumber) {
        return """
        You are an AI meeting assistant. Below is part %d of a transcribed meeting.

        Your task:
        - Summarize the content clearly and professionally
        - List key discussion points and decisions
        - Extract any action items with timestamps, e.g., "[00:02 - 00:05] John to email client."

        Transcript:
        %s
        """.formatted(partNumber, chunkText);
    }

    private String buildFinalSummary(List<String> partialSummaries) {
        StringBuilder prompt = new StringBuilder("""
        Based on the following summaries of a meeting, generate a final comprehensive summary.

        Be sure to:
        - Consolidate all content into one coherent summary
        - Emphasize key insights and decisions made
        - Clearly list all action items with timestamps (if mentioned)
        - Keep output under 400 words
        - Use bullet points for action items if possible

        ===
        """);

        for (int i = 0; i < partialSummaries.size(); i++) {
            prompt.append("Part ").append(i + 1).append(":\n").append(partialSummaries.get(i)).append("\n\n");
        }

        return simplifyWithPrompt(prompt.toString());
    }

    // Helper to call OpenAI with a custom prompt
    protected String simplifyWithPrompt(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIApiKey);

        Map<String, Object> message = Map.of(ROLE, USER, CONTENT, prompt);
        Map<String, Object> requestBody = Map.of(
                MODEL, GPT_3_5_TURBO_16_K,
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

    private List<String> chunkText(String fullText, int maxWordsPerChunk) {
        String[] words = fullText.split("\\s+");
        List<String> chunks = new ArrayList<>();

        for (int i = 0; i < words.length; i += maxWordsPerChunk) {
            int end = Math.min(i + maxWordsPerChunk, words.length);
            String chunk = String.join(" ", Arrays.copyOfRange(words, i, end));
            chunks.add(chunk);
        }

        return chunks;
    }
}
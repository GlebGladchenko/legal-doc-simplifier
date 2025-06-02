package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {
    private MockMvc mockMvc;

    @Mock
    private DocumentProcessingService documentProcessingService;

    @Mock
    OpenAIClientService openAIClientService;

    @InjectMocks
    private DocumentController documentController;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    }

    @Test
    @DisplayName("Should return 200 and simplified text when file is valid")
    void handleFileUpload_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );
        Mockito.when(documentProcessingService.extractTextFromFile(any())).thenReturn("Simplified text");
        Mockito.when(openAIClientService.callOpenAI(any())).thenReturn("Simplified text");

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Simplified text"));
    }

    @Test
    @DisplayName("Should return 400 when service throws IllegalArgumentException")
    void handleFileUpload_badRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );
        Mockito.when(documentProcessingService.extractTextFromFile(any()))
                .thenThrow(new IllegalArgumentException("Invalid file"));

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid file"));
    }

    @Test
    @DisplayName("Should return 500 when service throws generic Exception")
    void handleFileUpload_internalServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );
        Mockito.when(documentProcessingService.extractTextFromFile(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(multipart("/api/upload").file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Failed to extract text")));
    }
}
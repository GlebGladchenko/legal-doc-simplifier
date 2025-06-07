package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {
    private MockMvc mockMvc;

    @Mock
    private DocumentProcessingService processingService;

    @Mock
    OpenAIClientService openAIClientService;

    @InjectMocks
    private DocumentController documentController;

    private MockMultipartFile mockFile;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
        mockFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );
    }

    @Test
    void testIndexReturnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testHandleFileUploadReturnsResultViewAndModel() throws Exception {
        String extractedText = "Extracted text from PDF";
        String summary = "Simplified summary";

        when(processingService.extractTextFromFile(any())).thenReturn(extractedText);
        when(openAIClientService.callOpenAI(extractedText)).thenReturn(summary);

        mockMvc.perform(multipart("/upload")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("summary", summary));
    }
}
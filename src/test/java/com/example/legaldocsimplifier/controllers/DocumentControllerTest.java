package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.impl.DocumentProcessingServiceImpl;
import com.example.legaldocsimplifier.services.impl.OpenAIClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import com.example.legaldocsimplifier.models.IpUsage;
import org.springframework.test.web.servlet.request.RequestPostProcessor;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DocumentControllerTest {
    private MockMvc mockMvc;

    @Mock
    private DocumentProcessingServiceImpl processingService;

    @Mock
    OpenAIClientServiceImpl openAIClientService;

    @InjectMocks
    private DocumentController documentController;

    private MockMultipartFile mockFile;

    @BeforeEach
    public void init() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");
        mockMvc = MockMvcBuilders.standaloneSetup(documentController)
                .setViewResolvers(viewResolver)
                .build();
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
        String testIp = "127.0.0.1";
        int FREE_LIMIT = 5;

        // Mock IpUsage below free limit
        IpUsage ipUsage = new IpUsage();
        ipUsage.setIpAddress(testIp);
        ipUsage.setUsageCount(FREE_LIMIT - 4);

        when(processingService.getOrCreateIpUsage(any())).thenReturn(ipUsage);
        when(processingService.extractTextFromFile(any())).thenReturn(extractedText);
        when(openAIClientService.callOpenAI(extractedText)).thenReturn(summary);

        mockMvc.perform(multipart("/upload")
                        .file(mockFile)
                        .with(request -> { request.setRemoteAddr(testIp); return request; }))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("summary", summary));

        // Mock IpUsage at free limit (should return to index with error)
        IpUsage quotaExceededIpUsage = new IpUsage();
        quotaExceededIpUsage.setIpAddress(testIp);
        quotaExceededIpUsage.setUsageCount(FREE_LIMIT);

        when(processingService.getOrCreateIpUsage(any())).thenReturn(quotaExceededIpUsage);

        mockMvc.perform(multipart("/upload")
                        .file(mockFile)
                        .with(request -> { request.setRemoteAddr(testIp); return request; }))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void testPrivacyReturnsPrivacyView() throws Exception {
        mockMvc.perform(get("/privacy"))
                .andExpect(status().isOk())
                .andExpect(view().name("privacy"));
    }

    @Test
    void testTermsReturnsTermsView() throws Exception {
        mockMvc.perform(get("/terms"))
                .andExpect(status().isOk())
                .andExpect(view().name("terms"));
    }

    @Test
    void testDisclaimerReturnsDisclaimerView() throws Exception {
        mockMvc.perform(get("/disclaimer"))
                .andExpect(status().isOk())
                .andExpect(view().name("disclaimer"));
    }
}
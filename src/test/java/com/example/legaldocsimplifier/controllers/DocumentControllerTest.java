package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.impl.DocumentProcessingServiceImpl;
import com.example.legaldocsimplifier.services.impl.OpenAIClientServiceImpl;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import com.example.legaldocsimplifier.models.IpUsage;
import org.springframework.test.web.servlet.request.RequestPostProcessor;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    void testSuccessEndpointReturnsSuccessViewAndClientIp() throws Exception {
        String sessionId = "sess_123";
        String clientIp = "127.0.0.1";

        // Mock Stripe Session static retrieval
        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            Session mockSession = Mockito.mock(Session.class);
            when(mockSession.getClientReferenceId()).thenReturn(clientIp);
            sessionStatic.when(() -> Session.retrieve(sessionId)).thenReturn(mockSession);

            mockMvc.perform(get("/success").param("session_id", sessionId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("success"))
                    .andExpect(model().attribute("clientIp", clientIp));
        }
    }

    @Test
    void testCancelEndpointReturnsCancelView() throws Exception {
        mockMvc.perform(get("/cancel"))
                .andExpect(status().isOk())
                .andExpect(view().name("cancel"));
    }

    @Test
    void testShowContactFormReturnsContactView() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"));
    }

    @Test
    void testHandleContactFormReturnsContactViewWithSuccess() throws Exception {
        mockMvc.perform(post("/contact")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("message", "Hello!"))
                .andExpect(status().isOk())
                .andExpect(view().name("contact"))
                .andExpect(model().attribute("success", true));
    }
}
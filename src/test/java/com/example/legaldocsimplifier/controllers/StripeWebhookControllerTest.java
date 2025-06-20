package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.StripeWebHookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StripeWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private StripeWebHookService stripeWebHookService;

    @InjectMocks
    private StripeWebhookController stripeWebhookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(stripeWebhookController).build();
    }

    @Test
    void testHandleStripeEvent_Success() throws Exception {
        String payload = "{\"id\":\"evt_test\"}";
        String sigHeader = "test_signature";
        String expectedResponse = "Event processed";

        Mockito.when(stripeWebHookService.handleStripeEvent(payload, sigHeader)).thenReturn(expectedResponse);

        mockMvc.perform(post("/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Stripe-Signature", sigHeader))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));

        Mockito.verify(stripeWebHookService).handleStripeEvent(payload, sigHeader);
    }

    @Test
    void testHandleStripeEvent_Error() throws Exception {
        String payload = "{\"id\":\"evt_test\"}";
        String sigHeader = "test_signature";

        Mockito.when(stripeWebHookService.handleStripeEvent(payload, sigHeader))
                .thenThrow(new RuntimeException("Webhook error"));

        mockMvc.perform(post("/stripe/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Stripe-Signature", sigHeader))
                .andExpect(status().isOk());
    }
}
package org.novalegal.controllers;

import org.novalegal.services.PaymentService;
import com.stripe.exception.ApiException;
import com.stripe.model.checkout.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .build();
    }

    @Test
    void testCreateCheckoutSession_Success() throws Exception {
        String testIp = "127.0.0.1";
        String checkoutUrl = "https://checkout.stripe.com/pay/testsession";
        Session mockSession = Mockito.mock(Session.class);
        Mockito.when(mockSession.getUrl()).thenReturn(checkoutUrl);
        Mockito.when(paymentService.createCheckoutSession(anyString())).thenReturn(mockSession);

        mockMvc.perform(post("/api/payment/create-checkout-session")
                        .with(request -> {
                            request.setRemoteAddr(testIp);
                            return request;
                        }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(checkoutUrl));

        Mockito.verify(paymentService).createCheckoutSession(testIp);
    }

   @Test
    void testCreateCheckoutSession_StripeException() throws Exception {
        String testIp = "127.0.0.1";
        Mockito.when(paymentService.createCheckoutSession(anyString()))
                .thenThrow(new ApiException("Stripe error", null, null, 400, null));

        mockMvc.perform(post("/api/payment/create-checkout-session")
                        .with(request -> {
                            request.setRemoteAddr(testIp);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attribute("message", "Payment processing failed: Stripe error"));
    }
}
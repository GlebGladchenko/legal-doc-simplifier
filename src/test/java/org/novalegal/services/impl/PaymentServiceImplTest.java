package org.novalegal.services.impl;

import com.stripe.exception.ApiException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl();
        // Set the @Value fields via reflection for testing
        ReflectionTestUtils.setField(paymentService, "successUrl", "https://example.com/success");
        ReflectionTestUtils.setField(paymentService, "cancelUrl", "https://example.com/cancel");
    }

    @Test
    void testCreateCheckoutSession_Success() throws StripeException {
        String clientReferenceId = "test-client-id";
        Session mockSession = mock(Session.class);

        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            Session result = paymentService.createCheckoutSession(clientReferenceId);

            assertNotNull(result);
            sessionStatic.verify(() -> Session.create(any(SessionCreateParams.class)), times(1));
        }
    }

    @Test
    void testCreateCheckoutSession_StripeException() throws StripeException {
        String clientReferenceId = "test-client-id";

        try (MockedStatic<Session> sessionStatic = Mockito.mockStatic(Session.class)) {
            sessionStatic.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenThrow(new ApiException("Stripe error", null, null, 400, null));
            StripeException thrown = assertThrows(
                    StripeException.class,
                    () -> paymentService.createCheckoutSession(clientReferenceId)
            );
            assertEquals("Stripe error", thrown.getMessage());
        }
    }
}
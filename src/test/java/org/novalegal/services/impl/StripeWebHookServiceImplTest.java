package org.novalegal.services.impl;

import org.novalegal.dao.IpUsageRepository;
import org.novalegal.models.IpUsage;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StripeWebHookServiceImplTest {

    private StripeWebHookServiceImpl stripeWebHookService;
    private IpUsageRepository ipUsageRepository;

    @BeforeEach
    void setUp() {
        ipUsageRepository = mock(IpUsageRepository.class);
        stripeWebHookService = new StripeWebHookServiceImpl(ipUsageRepository);
        // Set the endpointSecret field via reflection for testing
        ReflectionTestUtils.setField(stripeWebHookService, "endpointSecret", "test_secret");
    }

    @Test
    void testHandleStripeEvent_CheckoutSessionCompleted_UpdatesIpUsage() {
        // Arrange
        String payload = "{...}"; // The actual payload is not used since we mock Webhook.constructEvent
        String sigHeader = "test_signature";

        // Mock Event and its behavior
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        // Simulate the JSON parsing inside the service
        // The service uses ObjectMapper to extract client_reference_id from the payload,
        // so we need to provide a payload that matches the expected structure.
        String testPayload = "{\n" +
                "  \"id\": \"evt_test\",\n" +
                "  \"type\": \"checkout.session.completed\",\n" +
                "  \"data\": {\n" +
                "    \"object\": {\n" +
                "      \"id\": \"cs_test_123\",\n" +
                "      \"client_reference_id\": \"127.0.0.1\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        IpUsage ipUsage = new IpUsage();
        ipUsage.setIpAddress("127.0.0.1");
        ipUsage.setUsageCount(3);
        ipUsage.setUsageLimit(2);

        when(ipUsageRepository.findByIpAddress("127.0.0.1")).thenReturn(Optional.of(ipUsage));

        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(testPayload, sigHeader, "test_secret"))
                    .thenReturn(mockEvent);

            // Act
            String result = stripeWebHookService.handleStripeEvent(testPayload, sigHeader);

            // Assert
            assertEquals("Event processed", result);
            ArgumentCaptor<IpUsage> captor = ArgumentCaptor.forClass(IpUsage.class);
            verify(ipUsageRepository).save(captor.capture());
            IpUsage savedUsage = captor.getValue();
            assertEquals(0, savedUsage.getUsageCount());
            assertEquals(5, savedUsage.getUsageLimit());
        }
    }

    @Test
void testHandleStripeEvent_NonCheckoutSessionCompleted_DoesNotUpdateIpUsage() throws Exception {
    // Arrange
    String payload = "{...}"; // The actual payload is not used since we mock Webhook.constructEvent
    String sigHeader = "test_signature";

    Event mockEvent = mock(Event.class);
    when(mockEvent.getType()).thenReturn("product.created");

    try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
        webhookStatic.when(() -> Webhook.constructEvent(payload, sigHeader, "test_secret"))
                .thenReturn(mockEvent);

        // Act
        String result = stripeWebHookService.handleStripeEvent(payload, sigHeader);

        // Assert
        assertEquals("Event processed", result);
        verify(ipUsageRepository, never()).save(any());
    }
}

    @Test
    void testHandleStripeEvent_SignatureVerificationException_ThrowsRuntimeException() {
        // Arrange
        String payload = "{}";
        String sigHeader = "test_signature";

        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(payload, sigHeader, "test_secret"))
                    .thenThrow(new SignatureVerificationException("Invalid signature", null));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> stripeWebHookService.handleStripeEvent(payload, sigHeader));
        }
    }

    @Test
    void testHandleStripeEvent_InvalidJson_ThrowsRuntimeException() {
        // Arrange
        String payload = "{invalid_json}";
        String sigHeader = "test_signature";
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn("checkout.session.completed");

        try (MockedStatic<Webhook> webhookStatic = Mockito.mockStatic(Webhook.class)) {
            webhookStatic.when(() -> Webhook.constructEvent(payload, sigHeader, "test_secret"))
                    .thenReturn(mockEvent);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> stripeWebHookService.handleStripeEvent(payload, sigHeader));
        }
    }
}
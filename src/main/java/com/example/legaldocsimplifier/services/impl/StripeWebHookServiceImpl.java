package com.example.legaldocsimplifier.services.impl;

import com.example.legaldocsimplifier.dao.IpUsageRepository;
import com.example.legaldocsimplifier.services.StripeWebHookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeWebHookServiceImpl implements StripeWebHookService {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final IpUsageRepository ipUsageRepository;

    public StripeWebHookServiceImpl(IpUsageRepository ipUsageRepository) {
        this.ipUsageRepository = ipUsageRepository;
    }

    @Override
    public String handleStripeEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            if ("checkout.session.completed".equals(event.getType())) {
                JsonNode json = new ObjectMapper().readTree(payload);
                String sessionId = json.get("data").get("object").get("id").asText();
                String clientRef = json.get("data").get("object").get("client_reference_id").asText();

                System.out.println("🎉 Session ID: " + sessionId);
                System.out.println("📍 Client Reference (IP): " + clientRef);

                // Upgrade the user in DB by IP
                ipUsageRepository.findByIpAddress(clientRef).ifPresent(usage -> {
                    usage.setUsageCount(0);
                    usage.setUsageLimit(5);
                    ipUsageRepository.save(usage);
                });
            }
        } catch (JsonProcessingException | SignatureVerificationException ex) {
            throw new RuntimeException(ex);
        }

        return "Event processed";
    }
}
package org.novalegal.services;

/**
 * Service interface for handling Stripe webhook events.
 */
public interface StripeWebHookService {
    /**
     * Handles the Stripe webhook event payload and signature.
     *
     * @param payload   The raw JSON payload from Stripe.
     * @param sigHeader The Stripe-Signature header.
     * @return A response message indicating the result.
     */
    String handleStripeEvent(String payload, String sigHeader);
}
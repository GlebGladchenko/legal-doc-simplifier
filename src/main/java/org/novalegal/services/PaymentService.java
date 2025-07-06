package org.novalegal.services;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

public interface PaymentService {
    /**
     * Creates a Stripe Checkout Session for the legal document simplification payment.
     *
     * @param clientReferenceId a unique identifier for the client (e.g., IP address)
     * @return the created Stripe Session
     * @throws StripeException if Stripe API call fails
     */
    Session createCheckoutSession(String clientReferenceId) throws StripeException;
}
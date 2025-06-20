package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.StripeWebHookService;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/stripe")
public class StripeWebhookController {

    private final StripeWebHookService stripeWebHookService;

    @Autowired
    public StripeWebhookController(StripeWebHookService stripeWebHookService) {
        this.stripeWebHookService = stripeWebHookService;
    }

    @ExceptionHandler(RuntimeException.class)
    public ModelAndView handleRuntimeExceptionException(RuntimeException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "Webhook failed: " + ex.getMessage());
        return mav;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeEvent(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        String result = stripeWebHookService.handleStripeEvent(payload, sigHeader);
        return ResponseEntity.ok(result);
    }
}
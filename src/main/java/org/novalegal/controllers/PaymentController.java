package org.novalegal.controllers;

import org.novalegal.services.PaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @ExceptionHandler(StripeException.class)
    public ModelAndView handleStripeException(StripeException ex) {
        ModelAndView mav = new ModelAndView("error");
        mav.addObject("message", "Payment processing failed: " + ex.getMessage());
        return mav;
    }

    @PostMapping("/create-checkout-session")
    public RedirectView createCheckoutSession(HttpServletRequest request) throws StripeException {
        String ip = request.getRemoteAddr();
        Session session = paymentService.createCheckoutSession(ip);
        return new RedirectView(session.getUrl());
    }
}
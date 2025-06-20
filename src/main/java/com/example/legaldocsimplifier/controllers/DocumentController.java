package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.models.IpUsage;
import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentController {
    private final DocumentProcessingService processingService;
    private final OpenAIClientService openAIClientService;

    public DocumentController(DocumentProcessingService processingService, OpenAIClientService openAIClientService) {
        this.processingService = processingService;
        this.openAIClientService = openAIClientService;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request, Model model) throws Exception {
        String ip = request.getRemoteAddr();
        IpUsage ipUsage = processingService.getOrCreateIpUsage(ip);

        if (ipUsage.getUsageCount() >= ipUsage.getUsageLimit()) {
            // Set error message for modal and a flag to trigger modal display
            model.addAttribute("error", "You have used your free quota. Please upgrade.");
            model.addAttribute("showLimitModal", true);
            return "index";
        }

        processingService.addUsage(ipUsage);

        String text = processingService.extractTextFromFile(file);
        String summary = openAIClientService.callOpenAI(text);
        model.addAttribute("summary", summary);
        return "result";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/disclaimer")
    public String disclaimer() {
        return "disclaimer";
    }

    @GetMapping("/success")
    public String success(@RequestParam("session_id") String sessionId, Model model) throws StripeException, StripeException {
        Session session = Session.retrieve(sessionId);
        String clientIp = session.getClientReferenceId();

        model.addAttribute("clientIp", clientIp);
        return "success";
    }

    @GetMapping("/cancel")
    public String cancel() {
        return "cancel";
    }

    @GetMapping("/contact")
    public String showContactForm() {
        return "contact";
    }

    @PostMapping("/contact")
    public String handleContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String message,
            Model model
    ) {
        // TODO: Send email or save to DB
        System.out.printf("📬 Contact form received: %s (%s) - %s%n", name, email, message);

        model.addAttribute("success", true);
        return "contact";
    }
}
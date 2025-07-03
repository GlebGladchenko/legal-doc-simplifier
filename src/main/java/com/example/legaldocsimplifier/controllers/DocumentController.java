package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.models.IpUsage;
import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.EmailService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentController {
    private final DocumentProcessingService processingService;
    private final OpenAIClientService openAIClientService;
    private final EmailService emailService;

    public DocumentController(DocumentProcessingService processingService,
                              OpenAIClientService openAIClientService,
                              EmailService emailService) {
        this.processingService = processingService;
        this.openAIClientService = openAIClientService;
        this.emailService = emailService;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(value = "useSample", required = false) String useSample,
                                   HttpServletRequest request,
                                   Model model) throws Exception {
        String ip = request.getRemoteAddr();
        IpUsage ipUsage = processingService.getOrCreateIpUsage(ip);

        /*if (ipUsage.getUsageCount() >= ipUsage.getUsageLimit()) {
            // Set error message for modal and a flag to trigger modal display
            model.addAttribute("error", "You have used your free quota. Please upgrade.");
            model.addAttribute("showLimitModal", true);
            return "index";
        }*/

        processingService.addUsage(ipUsage);

        if ("on".equals(useSample)) {
            ClassPathResource sample = new ClassPathResource("static/sample-nda.pdf");
            file = processingService.toMultipartFile(sample, "sample-nda.pdf", "application/pdf");
        }

        String text;
        try {
            text = processingService.extractTextFromFile(file);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage() != null ? ex.getMessage() : "Invalid or unsupported file uploaded.");
            model.addAttribute("showErrorModal", true);
            return "index";
        }

        String summary = openAIClientService.simplifyDocumentWithChunking(text, 4000);
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
        emailService.sendEmail(String.format("Name: %s, email: %s", name, email), message, email);

        model.addAttribute("success", true);
        return "contact";
    }
}
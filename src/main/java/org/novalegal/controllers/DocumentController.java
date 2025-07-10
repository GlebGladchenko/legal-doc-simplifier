package org.novalegal.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.novalegal.models.IpUsage;
import org.novalegal.services.DocumentProcessingService;
import org.novalegal.services.EmailService;
import org.novalegal.services.OpenAIClientService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

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
                                   HttpServletResponse response,
                                   Model model) throws Exception {

        // Get IP and headers
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        // Check for existing UUID cookie
        String uuid = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_uuid".equals(cookie.getName())) {
                    uuid = cookie.getValue();
                    break;
                }
            }
        }

        // If UUID cookie is missing, create and set it
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
            Cookie uuidCookie = new Cookie("user_uuid", uuid);
            uuidCookie.setPath("/");
            uuidCookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
            response.addCookie(uuidCookie);
        }

        // Fetch or create usage entry
        IpUsage usage = processingService.getOrCreateUsage(uuid, ip, userAgent, referer);

        // Usage limit logic (optional)
       /* if (usage.getUsageCount() >= usage.getUsageLimit()) {
            model.addAttribute("error", "You have used your free quota. Please upgrade.");
            model.addAttribute("showLimitModal", true);
            return "index";
        }*/

        // Record new usage
        processingService.addUsage(usage);

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
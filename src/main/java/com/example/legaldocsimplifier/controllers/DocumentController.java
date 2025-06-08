package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.models.IpUsage;
import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentController {
    private static final int FREE_LIMIT = 2;

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

        if (ipUsage.getUsageCount() >= FREE_LIMIT) {
            model.addAttribute("error", "You have used your free quota. Please upgrade.");
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
}

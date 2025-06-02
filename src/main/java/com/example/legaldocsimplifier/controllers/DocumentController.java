package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.OpenAIClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class DocumentController {
    private final DocumentProcessingService processingService;
    private final OpenAIClientService openAIClientService;

    public DocumentController(DocumentProcessingService processingService, OpenAIClientService openAIClientService) {
        this.processingService = processingService;
        this.openAIClientService = openAIClientService;
    }

    @PostMapping
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String text = processingService.extractTextFromFile(file);
            String result = openAIClientService.callOpenAI(text);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to extract text: " + e.getMessage());
        }
    }

}

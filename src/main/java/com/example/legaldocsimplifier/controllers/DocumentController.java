package com.example.legaldocsimplifier.controllers;

import com.example.legaldocsimplifier.services.DocumentProcessingService;
import com.example.legaldocsimplifier.services.DocumentTextExtractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class DocumentController {
    private final DocumentTextExtractionService extractionService;
    private final DocumentProcessingService processingService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public DocumentController(DocumentTextExtractionService extractionService, DocumentProcessingService processingService) {
        this.extractionService = extractionService;
        this.processingService = processingService;
    }

    @PostMapping
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String text = processingService.extractTextFromFile(file);
            return ResponseEntity.ok(text);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to extract text: " + e.getMessage());
        }
    }

}

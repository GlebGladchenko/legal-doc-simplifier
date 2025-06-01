package com.example.legaldocsimplifier.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentProcessingService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final DocumentTextExtractionService extractionService;

    public DocumentProcessingService(DocumentTextExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    public String extractTextFromFile(MultipartFile file) throws IllegalArgumentException, Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10MB limit.");
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name is missing.");
        }
        String lowerName = filename.toLowerCase();
        String contentType = file.getContentType();

        if (lowerName.endsWith(".pdf") && (contentType == null || contentType.equals("application/pdf"))) {
            return extractionService.extractTextFromPDF(file);
        } else if (lowerName.endsWith(".docx") && (contentType == null || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            return extractionService.extractTextFromDocx(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type. Only PDF and DOCX are allowed.");
        }
    }
}
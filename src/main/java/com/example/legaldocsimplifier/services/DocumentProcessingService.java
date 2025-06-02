package com.example.legaldocsimplifier.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentProcessingService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String NO_FILE_UPLOADED = "No file uploaded.";
    private static final String FILE_SIZE_EXCEEDS_10_MB_LIMIT = "File size exceeds 10MB limit.";
    private static final String FILE_NAME_IS_MISSING = "File name is missing.";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String WORDPROCESSINGML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String PDF_AND_DOCX_ARE_ALLOWED = "Unsupported file type. Only PDF and DOCX are allowed.";

    private final DocumentTextExtractionService extractionService;

    public DocumentProcessingService(DocumentTextExtractionService extractionService) {
        this.extractionService = extractionService;
    }

    public String extractTextFromFile(MultipartFile file) throws IllegalArgumentException, Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException(NO_FILE_UPLOADED);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(FILE_SIZE_EXCEEDS_10_MB_LIMIT);
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException(FILE_NAME_IS_MISSING);
        }
        String lowerName = filename.toLowerCase();
        String contentType = file.getContentType();

        if (lowerName.endsWith(".pdf") && (contentType == null || contentType.equals(CONTENT_TYPE))) {
            return extractionService.extractTextFromPDF(file);
        } else if (lowerName.endsWith(".docx") && (contentType == null
                || contentType.equals(WORDPROCESSINGML_DOCUMENT))) {
            return extractionService.extractTextFromDocx(file);
        } else {
            throw new IllegalArgumentException(PDF_AND_DOCX_ARE_ALLOWED);
        }
    }
}
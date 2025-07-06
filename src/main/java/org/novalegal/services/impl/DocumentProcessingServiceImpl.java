package org.novalegal.services.impl;

import org.novalegal.dao.IpUsageRepository;
import org.novalegal.models.IpUsage;
import org.novalegal.services.DocumentProcessingService;
import org.novalegal.services.DocumentTextExtractionService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;

@Service
public class DocumentProcessingServiceImpl implements DocumentProcessingService {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final String NO_FILE_UPLOADED = "No file uploaded.";
    private static final String FILE_SIZE_EXCEEDS_10_MB_LIMIT = "File size exceeds 10MB limit.";
    private static final String FILE_NAME_IS_MISSING = "File name is missing.";
    private static final String CONTENT_TYPE = "application/pdf";
    private static final String WORDPROCESSINGML_DOCUMENT = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String PDF_AND_DOCX_ARE_ALLOWED = "Unsupported file type. Only PDF and DOCX are allowed.";

    private final DocumentTextExtractionService extractionService;
    private final IpUsageRepository ipUsageRepository;

    public DocumentProcessingServiceImpl(DocumentTextExtractionService extractionService, IpUsageRepository ipUsageRepository) {
        this.extractionService = extractionService;
        this.ipUsageRepository = ipUsageRepository;
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

    public IpUsage getOrCreateIpUsage(String ip) {
        return ipUsageRepository.findByIpAddress(ip).orElseGet(() -> {
            IpUsage newUsage = new IpUsage();
            newUsage.setIpAddress(ip);
            newUsage.setUsageCount(0);
            newUsage.setLastUsed(LocalDateTime.now());
            return newUsage;
        });
    }

    public void addUsage(IpUsage ipUsage) {
        ipUsage.setUsageCount(ipUsage.getUsageCount() + 1);
        ipUsage.setLastUsed(LocalDateTime.now());
        ipUsageRepository.save(ipUsage);
    }

    public MultipartFile toMultipartFile(Resource resource, String fileName, String contentType) {
        byte[] content = null;
        try {
            content = resource.getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] finalContent = content;
        return new MultipartFile() {
            @Override public String getName() { return "file"; }

            @Override public String getOriginalFilename() { return fileName; }

            @Override public String getContentType() { return contentType; }

            @Override public boolean isEmpty() { return finalContent.length == 0; }

            @Override public long getSize() { return finalContent.length; }

            @Override public byte[] getBytes() { return finalContent; }

            @Override public InputStream getInputStream() {
                return new ByteArrayInputStream(finalContent);
            }

            @Override public void transferTo(File dest) throws IOException {
                try (OutputStream os = new FileOutputStream(dest)) {
                    os.write(finalContent);
                }
            }
        };
    }
}
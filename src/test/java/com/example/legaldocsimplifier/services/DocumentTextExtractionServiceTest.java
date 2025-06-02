package com.example.legaldocsimplifier.services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ExtendWith(MockitoExtension.class)
class DocumentTextExtractionServiceTest {
    @InjectMocks
    private DocumentTextExtractionService extractionService;

    @Test
    void testExtractTextFromPdf() throws IOException {
        // Create a simple PDF in memory
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            doc.addPage(new org.apache.pdfbox.pdmodel.PDPage());
            doc.save(out);
        }
        byte[] pdfBytes = out.toByteArray();

        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(pdfBytes));

        String text = extractionService.extractTextFromPDF(mockFile);
        // The text will be empty since the PDF has no content, but should not throw
        Assertions.assertNotNull(text);
    }

    @Test
    void testExtractTextFromDocx() throws IOException {
        // Create a simple DOCX in memory
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (XWPFDocument doc = new XWPFDocument()) {
            doc.createParagraph().createRun().setText("Hello DOCX");
            doc.write(out);
        }
        byte[] docxBytes = out.toByteArray();

        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream(docxBytes));

        String text = extractionService.extractTextFromDocx(mockFile);
        Assertions.assertTrue(text.contains("Hello DOCX"));
    }

    @Test
    void testExtractTextUnsupportedFileType() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("Some text".getBytes()));

        Assertions.assertThrows(IOException.class, () -> {
            extractionService.extractTextFromPDF(mockFile);
        });
    }

    @Test
    void testExtractTextIOException() throws IOException {
        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        Mockito.when(mockFile.getInputStream()).thenThrow(new IOException("IO error"));

        Assertions.assertThrows(IOException.class, () -> {
            extractionService.extractTextFromPDF(mockFile);
        });
    }
}
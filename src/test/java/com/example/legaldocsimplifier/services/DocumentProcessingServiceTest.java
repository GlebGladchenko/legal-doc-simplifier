package com.example.legaldocsimplifier.services;

import com.example.legaldocsimplifier.services.impl.DocumentProcessingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentProcessingServiceTest {
    @InjectMocks
    private DocumentProcessingServiceImpl documentProcessingService;
    @Mock
    private DocumentTextExtractionService extractionService;

    @Test
    void testExtractTextFromPdfFile_Success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");

        when(extractionService.extractTextFromPDF(mockFile)).thenReturn("PDF content");

        String result = documentProcessingService.extractTextFromFile(mockFile);
        assertEquals("PDF content", result);
        verify(extractionService).extractTextFromPDF(mockFile);
    }

    @Test
    void testExtractTextFromDocxFile_Success() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test.docx");
        when(mockFile.getContentType()).thenReturn("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        when(extractionService.extractTextFromDocx(mockFile)).thenReturn("DOCX content");

        String result = documentProcessingService.extractTextFromFile(mockFile);
        assertEquals("DOCX content", result);
        verify(extractionService).extractTextFromDocx(mockFile);
    }

    @Test
    void testExtractTextFromFile_EmptyFile() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                documentProcessingService.extractTextFromFile(mockFile));
        assertEquals("No file uploaded.", ex.getMessage());
    }

    @Test
    void testExtractTextFromFile_TooLarge() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(11 * 1024 * 1024L); // 11MB

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                documentProcessingService.extractTextFromFile(mockFile));
        assertEquals("File size exceeds 10MB limit.", ex.getMessage());
    }

    @Test
    void testExtractTextFromFile_UnsupportedFileType() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                documentProcessingService.extractTextFromFile(mockFile));
        assertEquals("Unsupported file type. Only PDF and DOCX are allowed.", ex.getMessage());
    }

    @Test
    void testExtractTextFromFile_NullFilename() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                documentProcessingService.extractTextFromFile(mockFile));
        assertEquals("File name is missing.", ex.getMessage());
    }
}

package com.example.legaldocsimplifier.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for extracting text content from legal document files.
 * <p>
 * Implementations of this interface provide methods to extract text from
 * supported file formats such as PDF and DOCX.
 */
public interface DocumentTextExtractionService {

    /**
     * Extracts text content from a PDF file.
     *
     * @param file the uploaded PDF file
     * @return the extracted text content as a String
     * @throws IOException if an I/O error occurs during extraction
     */
    String extractTextFromPDF(MultipartFile file) throws IOException;

    /**
     * Extracts text content from a DOCX file.
     *
     * @param file the uploaded DOCX file
     * @return the extracted text content as a String
     * @throws IOException if an I/O error occurs during extraction
     */
    String extractTextFromDocx(MultipartFile file) throws IOException;
}
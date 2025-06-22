package com.example.legaldocsimplifier.services;

/**
 * Service interface for interacting with the OpenAI API to simplify and summarize legal documents.
 */
public interface OpenAIClientService {


    /**
     * Simplifies and summarizes a legal document using a chunking approach to avoid token limits.
     * <p>
     * The document is split into chunks. Each chunk is sent to the OpenAI API sequentially, with the previous summary
     * provided as context for subsequent chunks. All chunk summaries are then merged and sent for a final simplification.
     * If the document fits in a single chunk, only one API call is made.
     *
     * @param doc       The full legal document text to simplify and summarize.
     * @param chunkSize The maximum number of words per chunk.
     * @return The fully simplified and summarized document as a single string.
     */
    String simplifyDocumentWithChunking(String doc, int chunkSize);
}
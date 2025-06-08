package com.example.legaldocsimplifier.services;

/**
 * Service interface for interacting with the OpenAI API to simplify and summarize legal documents.
 */
public interface OpenAIClientService {

    /**
     * Calls the OpenAI API to simplify and summarize the provided legal document text.
     *
     * @param documentText The text of the legal document to be simplified and summarized.
     * @return The simplified and summarized content as returned by OpenAI, or an error message if extraction fails.
     */
    String callOpenAI(String documentText);
}
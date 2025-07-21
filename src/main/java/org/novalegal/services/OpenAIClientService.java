package org.novalegal.services;

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

    /**
     * Generates a summarized meeting transcript by chunking Whisper transcription segments
     * and using the OpenAI GPT model for summarization.
     *
     * <p>This method takes a JSON string response from Whisper (e.g., returned from the
     * `/transcribe` endpoint of your GPU microservice), extracts the "segments" array,
     * chunks the transcript into smaller pieces to stay within token limits,
     * generates partial summaries for each chunk using {@code simplifyWithPrompt},
     * and finally combines those into a single final meeting summary.</p>
     *
     * <p>Each segment is annotated with a timestamp in the format
     * {@code [mm:ss - mm:ss]} before chunking, allowing the model to reference timing.
     * The chunk size is limited to ~12,000 characters to remain well under the
     * GPT-3.5-turbo-16k token limit (with final assembly).</p>
     *
     * @param whisperJson the full JSON string returned from Whisper's transcription,
     *                    expected to contain a top-level "segments" array
     * @return a concise final meeting summary generated from all segments,
     *         including decisions and action items
     * @throws IllegalArgumentException if "segments" is missing or malformed
     * @throws RuntimeException if parsing the JSON fails
     *
     */
    String generateMeetingSummaryFromSegmentsChunked(String whisperJson);
}
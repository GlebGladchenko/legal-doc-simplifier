package org.novalegal.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Service interface for processing meeting transcription jobs.
 */
public interface MeetingTranscriptionService {

    /**
     * Asynchronously processes an uploaded meeting video file:
     * <ul>
     *   <li>Extracts audio from the video</li>
     *   <li>Runs Whisper CLI for transcription</li>
     *   <li>Summarizes the transcript</li>
     *   <li>Updates the job status</li>
     * </ul>
     *
     * @param videoFile the uploaded video file to process
     * @param jobId the unique job ID associated with this meeting
     */
    void processMeetingFileAsync(File inputFile, String inputFileName, String jobId);
}

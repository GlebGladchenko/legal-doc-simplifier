package org.novalegal.services.impl;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.novalegal.models.MeetingJob;
import org.novalegal.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class MeetingTranscriptionServiceImpl implements MeetingTranscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingTranscriptionServiceImpl.class);
    private static final String SIGNED_URL = "signed_url";

    @Autowired
    private MeetingJobService jobService;
    @Autowired
    private OpenAIClientService openAIClientService;
    @Autowired
    private VideoProcessingService videoProcessingService;
    @Autowired
    private Storage storage;
    @Autowired
    private MeetingProcessingService processingService;

    @Value("${transcribe.api.key}")
    private String transcribeApiKey;
    @Value("${gcs.bucket.name}")
    private String bucketName;
    @Value("${modal.whisper.transcriber.url}")
    private String whisperAPI;

    @Async
    public void processMeetingFileAsync(String UUID, File inputFile, String inputFileName, String jobId) {
        MeetingJob job = jobService.getJob(jobId);
        job.setStatus("IN_PROGRESS");
        jobService.updateJob(jobId, job);

        try {
            // ✅ Extract audio and upload to GCS
            String gcsObjectName = videoProcessingService.extractAudioAndUploadToGCS(inputFile, inputFileName);

            // ✅ Generate signed URL or GCS path
            String gcsAudioUrl = generateSignedUrl(gcsObjectName);

            // ✅ Transcribe from GCS file using Whisper
            String transcript = transcribeFromGcsUrl(gcsAudioUrl);

            // ✅ Generate summary using OpenAI
            String summary = openAIClientService.generateMeetingSummaryFromSegmentsChunked(transcript);

            // ✅ Finalize job
            job.setStatus("COMPLETED");
            job.setSummaryText(summary);
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            logger.error(e.getMessage());
        }

        processingService.addJobStatus(UUID, job.getStatus());
        jobService.updateJob(jobId, job);
    }

    private String generateSignedUrl(String objectName) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();
        URL url = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES);  // 15-minute signed URL
        return url.toString();
    }

    private String transcribeFromGcsUrl(String audioUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", String.format("Bearer %s", transcribeApiKey));

        Map<String, String> body = Map.of(SIGNED_URL, audioUrl);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(whisperAPI, request, String.class);
        return response.getBody();
    }
}

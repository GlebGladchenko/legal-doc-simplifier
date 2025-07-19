package org.novalegal.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.novalegal.models.MeetingJob;
import org.novalegal.services.MeetingJobService;
import org.novalegal.services.MeetingTranscriptionService;
import org.novalegal.util.VideoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.Set;

@Controller
public class MeetingSummarizerController {
    private static final Set<String> ALLOWED_VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/x-matroska",  // .mkv
            "video/webm",
            "video/quicktime"    // .mov
    );

    private final MeetingTranscriptionService transcriptionService;
    private final MeetingJobService jobService;

    @Autowired
    public MeetingSummarizerController(MeetingTranscriptionService transcriptionService, MeetingJobService jobService) {
        this.transcriptionService = transcriptionService;
        this.jobService = jobService;
    }

    @GetMapping("/meeting-summarizer")
    public String showUploadForm(Model model) {
        return "meeting-summarizer/meeting-summarizer"; // Thymeleaf template name (meeting-summarizer.html)
    }

    @PostMapping("/meeting-summarizer")
    public ResponseEntity<?> processMeetingUpload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + "File is empty");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_VIDEO_TYPES.contains(mimeType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported file format: " + mimeType + ". Please upload an MP4, MKV, WebM, or MOV file.");
        }

        String inputFileName = VideoUtils.generateTempFilename(file.getOriginalFilename());
        File inputFile = new File(inputFileName);

        try {
            file.transferTo(inputFile);

            String jobId = jobService.createJob();
            transcriptionService.processMeetingFileAsync(inputFile, inputFileName, jobId);

            // Detect if it's an AJAX request and respond with JSON
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                return ResponseEntity.ok(Map.of("redirect", "/meeting-summarizer/status/" + jobId));
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/meeting-summarizer/status/" + jobId)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/meeting-summarizer/status/{jobId}")
    public String getJobStatus(@PathVariable String jobId, Model model) {
        MeetingJob job = jobService.getJob(jobId);

        if (job == null) {
            model.addAttribute("error", "Job not found for ID: " + jobId);
            return "meeting-summarizer";
        }

        model.addAttribute("job", job);
        return "meeting-summarizer/meeting-summarizer-status";
    }
}
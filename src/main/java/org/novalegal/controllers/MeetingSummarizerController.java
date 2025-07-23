package org.novalegal.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.novalegal.models.IpUsage;
import org.novalegal.models.MeetingJob;
import org.novalegal.models.MeetingSummarizerUsage;
import org.novalegal.services.MeetingJobService;
import org.novalegal.services.MeetingProcessingService;
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
import java.util.UUID;

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
    private final MeetingProcessingService processingService;

    @Autowired
    public MeetingSummarizerController(MeetingTranscriptionService transcriptionService,
                                       MeetingJobService jobService,
                                       MeetingProcessingService processingService) {
        this.transcriptionService = transcriptionService;
        this.jobService = jobService;
        this.processingService = processingService;
    }

    @GetMapping("/meeting-summarizer")
    public String showUploadForm(Model model) {
        return "meeting-summarizer/meeting-summarizer"; // Thymeleaf template name (meeting-summarizer.html)
    }

    @GetMapping("/meeting-summarizer/on-premise")
    public String onPremise() {
        return "on-premise";
    }

    @PostMapping("/meeting-summarizer")
    public ResponseEntity<?> processMeetingUpload(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + "File is empty");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_VIDEO_TYPES.contains(mimeType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Unsupported file format: " + mimeType + ". Please upload an MP4, MKV, WebM, or MOV file.");
        }

        // Get IP and headers
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");

        // Check for existing UUID cookie
        String uuid = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("summarizer_uuid".equals(cookie.getName())) {
                    uuid = cookie.getValue();
                    break;
                }
            }
        }

        // If UUID cookie is missing, create and set it
        if (uuid == null || uuid.isBlank()) {
            uuid = UUID.randomUUID().toString();
            Cookie uuidCookie = new Cookie("summarizer_uuid", uuid);
            uuidCookie.setPath("/");
            uuidCookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
            response.addCookie(uuidCookie);
        }

        // Fetch or create usage entry
        MeetingSummarizerUsage usage = processingService.getOrCreateUsage(uuid, ip, userAgent, referer);
        processingService.addUsage(usage);

        String inputFileName = VideoUtils.generateTempFilename(file.getOriginalFilename());
        File inputFile = new File(inputFileName);

        try {
            file.transferTo(inputFile);

            String jobId = jobService.createJob();
            transcriptionService.processMeetingFileAsync(uuid, inputFile, inputFileName, jobId);

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
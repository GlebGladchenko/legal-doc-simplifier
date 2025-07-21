package org.novalegal.models;

import java.time.Instant;

public class MeetingJob {

    private String jobId;
    private String status; // PENDING, IN_PROGRESS, COMPLETED, FAILED
    private String summaryText;
    private String errorMessage;
    private Instant createdAt;

    public MeetingJob(String jobId) {
        this.jobId = jobId;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    // Getters and setters
    public String getJobId() { return jobId; }
    public String getStatus() { return status; }
    public String getSummaryText() { return summaryText; }
    public String getErrorMessage() { return errorMessage; }

    public void setStatus(String status) { this.status = status; }
    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

package org.novalegal.services;

import org.novalegal.models.MeetingSummarizerUsage;

public interface MeetingProcessingService {

    MeetingSummarizerUsage getOrCreateUsage(String ip);

    void addUsage(MeetingSummarizerUsage meetingSummarizerUsage);

    MeetingSummarizerUsage getOrCreateUsage(String uuid, String ip, String userAgent, String referer);

    void addJobStatus(String uuid, String status);
}

package org.novalegal.services.impl;

import org.novalegal.dao.MeetingSummarizerUsageRepository;
import org.novalegal.models.MeetingSummarizerUsage;
import org.novalegal.services.MeetingProcessingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MeetingProcessingServiceImpl implements MeetingProcessingService {
    private final MeetingSummarizerUsageRepository summarizerUsageRepository;

    public MeetingProcessingServiceImpl(MeetingSummarizerUsageRepository summarizerUsageRepository) {
        this.summarizerUsageRepository = summarizerUsageRepository;
    }

    public MeetingSummarizerUsage getOrCreateUsage(String ip) {
        return summarizerUsageRepository.findByIpAddress(ip).orElseGet(() -> {
            MeetingSummarizerUsage newUsage = new MeetingSummarizerUsage();
            newUsage.setIpAddress(ip);
            newUsage.setUsageCount(0);
            newUsage.setLastUsed(LocalDateTime.now());
            return newUsage;
        });
    }

    public void addUsage(MeetingSummarizerUsage meetingSummarizerUsage) {
        meetingSummarizerUsage.setUsageCount(meetingSummarizerUsage.getUsageCount() + 1);
        meetingSummarizerUsage.setLastUsed(LocalDateTime.now());
        summarizerUsageRepository.save(meetingSummarizerUsage);
    }

    public MeetingSummarizerUsage getOrCreateUsage(String uuid, String ip, String userAgent, String referer) {
        Optional<MeetingSummarizerUsage> found;

        if (uuid != null && !uuid.isEmpty()) {
            found = summarizerUsageRepository.findByUuid(uuid);
        } else {
            found = summarizerUsageRepository.findFirstByIpAddressAndUserAgentAndReferer(ip, userAgent, referer);
        }

        if (found.isPresent()) {
            MeetingSummarizerUsage usage = found.get();
            usage.setLastUsed(LocalDateTime.now());
            return summarizerUsageRepository.save(usage); // Update last used time
        }

        // New user
        MeetingSummarizerUsage usage = new MeetingSummarizerUsage();
        usage.setUuid(uuid); // may be null
        usage.setIpAddress(ip);
        usage.setUserAgent(userAgent);
        usage.setReferer(referer);
        usage.setUsageCount(0);
        usage.setUsageLimit(2); // Or pull from config
        usage.setLastUsed(LocalDateTime.now());

        return summarizerUsageRepository.save(usage);
    }

    public void addJobStatus(String uuid, String status) {
        Optional<MeetingSummarizerUsage> found = summarizerUsageRepository.findByUuid(uuid);

        if (found.isPresent()) {
            MeetingSummarizerUsage usage = found.get();
            usage.setJobStatus(status);
            summarizerUsageRepository.save(usage);
        }
    }
}

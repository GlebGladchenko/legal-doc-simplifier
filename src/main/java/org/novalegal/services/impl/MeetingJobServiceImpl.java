package org.novalegal.services.impl;


import org.novalegal.models.MeetingJob;
import org.novalegal.services.MeetingJobService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MeetingJobServiceImpl implements MeetingJobService {

    private final Map<String, MeetingJob> jobs = new ConcurrentHashMap<>();

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, new MeetingJob(jobId));
        return jobId;
    }

    public MeetingJob getJob(String jobId) {
        return jobs.get(jobId);
    }

    public void updateJob(String jobId, MeetingJob updatedJob) {
        jobs.put(jobId, updatedJob);
    }
}

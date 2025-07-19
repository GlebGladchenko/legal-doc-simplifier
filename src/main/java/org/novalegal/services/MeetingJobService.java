package org.novalegal.services;

import org.novalegal.models.MeetingJob;

/**
 * Service interface for managing meeting jobs.
 */
public interface MeetingJobService {

    /**
     * Creates a new meeting job and returns its unique job ID.
     *
     * @return the unique job ID for the newly created meeting job
     */
    String createJob();

    /**
     * Retrieves a meeting job by its job ID.
     *
     * @param jobId the unique identifier of the meeting job
     * @return the MeetingJob object if found, or null if not found
     */
    MeetingJob getJob(String jobId);

    /**
     * Updates the meeting job with the specified job ID.
     *
     * @param jobId the unique identifier of the meeting job to update
     * @param updatedJob the updated MeetingJob object
     */
    void updateJob(String jobId, MeetingJob updatedJob);
}

package org.novalegal.services;

import org.novalegal.models.IpUsage;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for processing legal documents and managing IP usage.
 */
public interface DocumentProcessingService {

    /**
     * Extracts text content from the provided file.
     *
     * @param file the uploaded file (PDF or DOCX)
     * @return the extracted text content
     * @throws IllegalArgumentException if the file is invalid or unsupported
     * @throws Exception if extraction fails for other reasons
     */
    String extractTextFromFile(MultipartFile file) throws IllegalArgumentException, Exception;

    /**
     * Retrieves the IpUsage record for the given IP address, or creates a new one if it does not exist.
     *
     * @param ip the IP address to look up
     * @return the existing or newly created IpUsage record
     */
    IpUsage getOrCreateIpUsage(String ip);

    /**
     * Increments the usage count and updates the last used timestamp for the given IpUsage record.
     *
     * @param ipUsage the IpUsage record to update
     */
    void addUsage(IpUsage ipUsage);

    MultipartFile toMultipartFile(Resource resource, String fileName, String contentType);
}
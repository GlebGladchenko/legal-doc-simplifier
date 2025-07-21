package org.novalegal.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface VideoProcessingService {

    String extractAudioAndUploadToGCS(File inputFile, String inputFileName) throws Exception;
}

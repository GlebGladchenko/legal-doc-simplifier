package org.novalegal.services.impl;

import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.novalegal.services.VideoProcessingService;
import org.novalegal.util.VideoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.UUID;

@Service
public class VideoProcessingServiceImpl implements VideoProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingTranscriptionServiceImpl.class);
    private static final int BUFFER_SIZE = 8192;

    private final Storage storage;

    @Value("${gcs.bucket.name}")
    private String bucketName;

    public VideoProcessingServiceImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public String extractAudioAndUploadToGCS(File inputFile, String inputFileName) throws Exception {
        String outputFileName = "/tmp/output-" + UUID.randomUUID() + ".mp3";

        try {
            // Run FFmpeg to convert to MP3
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-i", inputFileName,
                    "-f", "mp3", "-ab", "192k", "-y", outputFileName
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Optional: log ffmpeg output
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("FFmpeg exited with code " + exitCode);
            }

            // Upload output file to GCS
            String objectName = "audio-" + UUID.randomUUID() + ".mp3";
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName).build();

            try (
                    FileInputStream outputStream = new FileInputStream(outputFileName);
                    WriteChannel gcsWriter = storage.writer(blobInfo)
            ) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = outputStream.read(buffer)) != -1) {
                    gcsWriter.write(ByteBuffer.wrap(buffer, 0, len));
                }
            }

            Blob blob = storage.get(blobInfo.getBlobId());
            if (blob == null || blob.getSize() < 10_000) {
                throw new IOException("Output audio is too small. Possibly failed.");
            }

            return objectName;

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            if (inputFile.exists()) inputFile.delete();
            File outputFile = new File(outputFileName);
            if (outputFile.exists()) outputFile.delete();
        }
    }
}

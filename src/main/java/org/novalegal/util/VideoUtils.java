package org.novalegal.util;

import java.util.UUID;

public class VideoUtils {

    public static String generateTempFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        if (!isSupportedFormat(extension)) {
            throw new IllegalArgumentException("Unsupported video format: " + extension);
        }
        return "/tmp/input-" + UUID.randomUUID() + "." + extension;
    }

    private static String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "mp4"; // default fallback
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private static boolean isSupportedFormat(String extension) {
        return extension.equals("mp4") || extension.equals("mkv") ||
                extension.equals("webm") || extension.equals("mov");
    }
}

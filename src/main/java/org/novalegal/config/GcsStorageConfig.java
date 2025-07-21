package org.novalegal.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class GcsStorageConfig {

    @Bean
    public Storage googleCloudStorage() throws Exception {
        String jsonString = System.getenv("GCP_CREDENTIALS_JSON");
        String projectId = System.getenv("GCP_PROJECT_ID"); // Optional if in JSON

        StorageOptions.Builder builder = StorageOptions.newBuilder();

        if (jsonString != null && !jsonString.isEmpty()) {
            // ✅ Railway or custom env var
            try (InputStream is = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                builder.setCredentials(credentials);
            }
        } else {
            // ✅ Local dev (uses GOOGLE_APPLICATION_CREDENTIALS env var)
            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
            builder.setCredentials(credentials);
        }

        if (projectId != null && !projectId.isEmpty()) {
            builder.setProjectId(projectId);
        }

        return builder.build().getService();
    }
}

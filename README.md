# Legal Document Simplifier & Meeting Summarizer

A Spring Boot application that helps users process and understand documents and recordings. It supports:
- Uploading and parsing PDFs and Word (.docx) files
- Web UI with Thymeleaf
- Persisting data via JPA/Hibernate
- PostgreSQL in production; H2 for tests
- Payments via Stripe
- Email notifications via Spring Mail
- File storage via Google Cloud Storage (GCS)
- Large file uploads (up to 1GB)
- Asynchronous tasks (e.g., background processing)
- Optional audio transcription via an external Whisper/Modal service

## Table of Contents
- Features
- Meeting Summarizer
  - Overview
  - Supported Formats
  - Endpoints
  - Processing Flow
  - UI Templates
  - Configuration Notes
  - Troubleshooting (Summarizer)
- GPU Whisper Transcription (Modal)
  - What It Is
  - Endpoint and Auth
  - How It Works
  - Spring Integration Flow
  - Environment and Deployment Notes
  - Modal GPU Processing Details
- Architecture & Tech Stack
- Getting Started
  - Prerequisites
  - Configuration
  - Running locally
- Build & Test
- Docker & docker-compose
- Environment Variables
- Project Structure
- Troubleshooting
- License

## Features

- Legal Document Simplifier
  - Upload PDFs and Word (.docx) files
  - Extract and parse content using Apache PDFBox and Apache POI
  - Prepare text for downstream processing (simplification, analysis)
  - Integrates with persistence and optional cloud storage (GCS)

- Meeting Summarizer
  - Upload meeting recordings in MP4/MKV/WebM/MOV
  - AI-driven transcription and summarization with asynchronous processing and job tracking
  - Clear upload UI (Thymeleaf), supported formats, and privacy notes
  - Optional GPU acceleration via a Modal-hosted Whisper microservice for long audio

- Platform capabilities
  - Web APIs and server-side rendering using Spring Web and Thymeleaf
  - Persistence with Spring Data JPA and PostgreSQL (H2 for tests)
  - Stripe payment integration
  - Email sending (SMTP) via Spring Mail
  - GCS-backed storage for uploads or processed artifacts
  - Async processing with Spring’s @EnableAsync
  - Configurable external transcription service endpoint for audio/video files

## Meeting Summarizer

### Overview
A web UI and backend workflow to accept meeting recordings (video) and produce an AI-assisted transcript and summary. The feature is designed for privacy-conscious usage and supports large uploads. Jobs run asynchronously, and users can revisit a status page to view progress/results.

- Entry points:
  - GET /meeting-summarizer — Upload form (Thymeleaf)
  - POST /meeting-summarizer — Starts background processing for an uploaded video
  - GET /meeting-summarizer/status/{jobId} — Status page for a submitted job
  - GET /meeting-summarizer/on-premise — Informational page about on-prem usage

- Usage tracking:
  - A persistent cookie summarizer_uuid (1 year) associates uploads with a pseudo-identifier.
  - The system records IP, User-Agent, and Referer to aggregate usage via MeetingProcessingService.

### Supported Formats
- MP4: video/mp4
- MKV: video/x-matroska
- WebM: video/webm
- MOV: video/quicktime

Server validates MIME types and returns clear errors for unsupported formats or empty files.

### Endpoints
- GET /meeting-summarizer
  - Renders the upload page with supported formats and privacy notes.
- POST /meeting-summarizer
  - Accepts multipart form field file.
  - On success, redirects to /meeting-summarizer/status/{jobId} or returns JSON { "redirect": "/meeting-summarizer/status/{jobId}" } for AJAX.
  - On validation error: returns 400 (unsupported format) or 500 (empty file/processing error) with a descriptive message.
- GET /meeting-summarizer/status/{jobId}
  - Renders current job state or a friendly error if not found.
- GET /meeting-summarizer/on-premise
  - Static informational page.

### Processing Flow
1) User opens GET /meeting-summarizer and uploads a recording.
2) Controller validates MIME type and non-emptiness.
3) Ensures a summarizer_uuid cookie is present (sets one if missing).
4) Records usage (IP, User-Agent, Referer) through MeetingProcessingService.
5) Writes the upload to a temporary file (e.g., VideoUtils.generateTempFilename).
6) Creates a job via MeetingJobService and starts asynchronous processing in MeetingTranscriptionService.
7) Returns a redirect (or JSON redirect) to GET /meeting-summarizer/status/{jobId}.
8) The status page uses MeetingJobService to show progress/results; errors are handled gracefully.

Primary components:
- Controller: org.novalegal.controllers.MeetingSummarizerController
- Services:
  - MeetingJobService — job creation/retrieval
  - MeetingTranscriptionService — async transcription and summarization
  - MeetingProcessingService — usage aggregation and orchestration metadata

### UI Templates
- templates/meeting-summarizer/meeting-summarizer.html — Upload form, supported formats, privacy notes
- templates/meeting-summarizer/meeting-summarizer-status.html — Status/details view

### Configuration Notes
- Large uploads:
  - spring.servlet.multipart.max-file-size=1GB
  - spring.servlet.multipart.max-request-size=1GB
- External transcription (optional):
  - TRANSCRIBE_API_KEY
  - MODAL_WHISPER_TRANSCRIBER_URL
- Storage (optional):
  - GCS_BUCKET_NAME
  - GOOGLE_APPLICATION_CREDENTIALS must point to a service account JSON with Storage permissions.

### Troubleshooting (Summarizer)
- Unsupported file format:
  - Ensure the upload is mp4/mkv/webm/mov and the browser sets a proper MIME type.
- “File is empty”:
  - Verify the form field name is file, and a file is selected.
- “Job not found”:
  - The jobId might be incorrect or expired; retry the upload and use the provided status link.
- Cookie missing:
  - Browser must allow cookies for summarizer_uuid to persist.

## GPU Whisper Transcription (Modal)

This project integrates a serverless GPU microservice on Modal to transcribe audio using OpenAI Whisper, which accelerates long audio processing and offloads heavy compute.

### What It Is
- Location: src/main/resources/modal/modal_whisper_app.py
- Tech: Modal serverless + FastAPI with a custom image that installs ffmpeg and Whisper.
- Model: Whisper "base" (can be changed to small/medium/large depending on accuracy/latency needs).
- Hardware: Runs on a GPU (A10G) for faster inference.

### Endpoint and Auth
- FastAPI route exposed by Modal:
  - POST /transcribe-gcs
  - Auth: Bearer token header Authorization: Bearer <TRANSCRIBE_API_KEY>
- Request body:
  - { "signed_url": "<GCS signed URL to an .mp3 file>" }
- Response:
  - Whisper transcription JSON (segments, text, timings, etc.)

Related environment variables in the main app:
- TRANSCRIBE_API_KEY — shared secret used by the Java service to call the Modal endpoint.
- MODAL_WHISPER_TRANSCRIBER_URL — full HTTPS URL to the Modal ASGI app (e.g., https://...modal.run/transcribe-gcs).

### How It Works
- The Modal image installs:
  - fastapi, uvicorn, torch, ffmpeg, python-multipart, requests, and whisper (git).
- The GPU function transcribe_signed_url_mp3:
  - Downloads the audio using the provided signed URL (streaming via requests).
  - Saves to a temporary .mp3 file.
  - Loads the Whisper model and runs model.transcribe(tmp_file).
  - Returns the raw transcription result as JSON.

- The ASGI app mounts FastAPI and exposes /transcribe-gcs, validating the Bearer token against TRANSCRIBE_API_KEY.

### Spring Integration Flow
In MeetingTranscriptionServiceImpl (Java):
- Extract audio from uploaded video and upload to GCS (VideoProcessingService.extractAudioAndUploadToGCS).
- Generate a short-lived signed URL to the GCS object (Storage.signUrl, e.g., 15 minutes).
- Call the Modal endpoint with the signed URL:
  - POST MODAL_WHISPER_TRANSCRIBER_URL
  - Headers: Content-Type: application/json, Authorization: Bearer TRANSCRIBE_API_KEY
  - Body: { "signed_url": "<signed GCS URL>" }
- Receive the Whisper transcription (JSON) and feed it to OpenAI summarization:
  - openAIClientService.generateMeetingSummaryFromSegmentsChunked(transcript)
- Update the MeetingJob status and persist summary.

Key Java file:
- src/main/java/org/novalegal/services/impl/MeetingTranscriptionServiceImpl.java

### Environment and Deployment Notes
- Required environment variables (example values can be placed in .env for local dev):
  - TRANSCRIBE_API_KEY=... (same value deployed to Modal secret and Java env)
  - MODAL_WHISPER_TRANSCRIBER_URL=https://<your-app>--whisper-transcriber-gpu-fastapi-app.modal.run/transcribe-gcs
  - GCS_BUCKET_NAME=...
  - GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account.json
- The Modal function uses a named secret whisper-auth-key if you opt to store keys on Modal. Ensure it contains TRANSCRIBE_API_KEY (and any others you require).
- Audio format: MeetingTranscriptionService extracts audio and sends a signed URL to an .mp3 object (ffmpeg conversion happens in your video processing step).
- Security: Signed URLs expire; keep the timeout balanced (e.g., 15 minutes) with queue/transcription time. Ensure the Modal timeout is adequate (function timeout=300s in the Python code).

### Modal GPU Processing Details
- Image and runtime:
  - Based on debian-slim + Python 3.10 with CUDA-enabled PyTorch on Modal
  - Includes ffmpeg for decoding and resampling, ensuring stable Whisper input
- Performance considerations:
  - A10G GPU for fast inference; consider batching longer files or switching model size for accuracy/speed trade-offs
  - Cold starts: Modal warms containers; first request may be slower
  - Network: Transcription pulls from a signed GCS URL; ensure URL validity window > expected queue + processing time
- Security and access:
  - API key-based auth via Bearer token
  - Signed URLs avoid exposing public buckets and expire automatically
- Operability:
  - Health and logs via Modal dashboard
  - Increase function timeout for long files; ensure Spring client timeouts accommodate worst-case durations

## Architecture & Tech Stack
- Language: Java 17
- Framework: Spring Boot 3.5.x
  - spring-boot-starter (core)
  - spring-boot-starter-web
  - spring-boot-starter-thymeleaf
  - spring-boot-starter-data-jpa
  - spring-boot-starter-mail
- Persistence: Hibernate/JPA, PostgreSQL driver (test: H2)
- File processing: Apache PDFBox, Apache POI (OOXML)
- Cloud Storage: Google Cloud Storage Java SDK
- Payments: stripe-java
- Build: Gradle
- Templates: Thymeleaf (e.g., templates/blog/index.html)
- Async: Enabled in the main application class

Entry point:
- org.novalegal.LegalDocSimplifierApplication (Spring Boot, @EnableAsync)

## Getting Started

### Prerequisites
- Java 17 (check with `java -version`)
- Gradle wrapper included (use `./gradlew` or `gradlew.bat`)
- A PostgreSQL database (for non-test runs) or Docker (to use docker-compose)
- Google Cloud credentials if using GCS
- SMTP credentials if using email features
- Stripe API keys if using payments

### Configuration
Application configuration is externalized via environment variables and Spring properties.

Key properties (src/main/resources/application.properties):
- spring.jpa.hibernate.ddl-auto=update
- spring.jpa.show-sql=true
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
- spring.datasource.url=${SPRING_DATASOURCE_URL}
- spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
- spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
- stripe.success.url=${STRIPE_SUCCESS_URL}
- stripe.cancel.url=${STRIPE_CANCEL_URL}
- spring.servlet.multipart.max-file-size=1GB
- spring.servlet.multipart.max-request-size=1GB
- server.port=${PORT:8080}
- transcriber.api.key=${TRANSCRIBE_API_KEY}
- gcs.bucket.name=${GCS_BUCKET_NAME}
- modal.whisper.transcriber.url=${MODAL_WHISPER_TRANSCRIBER_URL}

Mail:
- spring.mail.host=${MAIL_HOST}
- spring.mail.port=${MAIL_PORT}
- spring.mail.username=${MAIL_USERNAME}
- spring.mail.password=${MAIL_PASSWORD}
- spring.mail.from=${MAIL_FROM}
- spring.mail.to=${MAIL_TO}
- spring.mail.properties.mail.smtp.auth=true
- spring.mail.properties.mail.smtp.starttls.enable=true

Note:
- Provide Stripe keys via env (STRIPE_API_KEY, STRIPE_WEBHOOK_SECRET) and success/cancel URLs.
- Provide GCS credentials via GOOGLE_APPLICATION_CREDENTIALS path to a service account JSON.

### Running locally

Option A: Run against a local/remote PostgreSQL
1) Export environment variables (example):
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/legal_docs
- SPRING_DATASOURCE_USERNAME=postgres
- SPRING_DATASOURCE_PASSWORD=postgres
- STRIPE_API_KEY=sk_test_xxx
- STRIPE_WEBHOOK_SECRET=whsec_xxx
- STRIPE_SUCCESS_URL=http://localhost:8080/success
- STRIPE_CANCEL_URL=http://localhost:8080/cancel
- MAIL_HOST=smtp.example.com
- MAIL_PORT=587
- MAIL_USERNAME=your-user
- MAIL_PASSWORD=your-pass
- MAIL_FROM=no-reply@example.com
- MAIL_TO=you@example.com
- GCS_BUCKET_NAME=your-bucket
- GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/sa.json
- TRANSCRIBE_API_KEY=your-transcriber-api-key
- MODAL_WHISPER_TRANSCRIBER_URL=https://your-modal-whisper-url
- PORT=8080

2) Build and run:
- ./gradlew clean bootRun

Option B: Run the built jar
- ./gradlew bootJar
- java -jar build/libs/legal-doc-simplifier-0.0.1-SNAPSHOT.jar

Access:
- Web/UI: http://localhost:8080
- If you expose any REST endpoints, they will be under the same base URL.

## Build & Test
- Build: ./gradlew clean build
- Run tests: ./gradlew test
  - Uses JUnit 5 and Spring Boot test starter
  - H2 is available for tests via testImplementation

## Docker & docker-compose
A docker-compose.yml is provided with environment variables for the app and a PostgreSQL service.

Typical services:
- db: PostgreSQL
- app: This Spring Boot app

Important environment variables exposed in docker-compose for the app:
- SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/${POSTGRES_DB}
- SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
- SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
- STRIPE_API_KEY, STRIPE_WEBHOOK_SECRET
- OPENAI_API_KEY (if applicable for other features)
- MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM, MAIL_TO
- STRIPE_SUCCESS_URL, STRIPE_CANCEL_URL
- TRANSCRIBE_API_KEY
- GCS_BUCKET_NAME
- GOOGLE_APPLICATION_CREDENTIALS=/app/secrets/your-credentials.json
- MODAL_WHISPER_TRANSCRIBER_URL
- JAVA_TOOL_OPTIONS for remote debug on :5005

To run with docker-compose:
- docker compose up --build

To enable remote debugging:
- Ensure JAVA_TOOL_OPTIONS includes the JDWP agent and map port 5005.

## Environment Variables
Minimum set to run the app (adjust as needed):
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- PORT (optional, defaults to 8080)
- STRIPE_SUCCESS_URL, STRIPE_CANCEL_URL (if Stripe is used)
- STRIPE_API_KEY (for Stripe client)
- MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM (for email)
- GCS_BUCKET_NAME and GOOGLE_APPLICATION_CREDENTIALS (for GCS)
- TRANSCRIBE_API_KEY and MODAL_WHISPER_TRANSCRIBER_URL (for transcription integration)

## Project Structure
Key files/folders (non-exhaustive):
- build.gradle — Gradle build script and dependencies
- src/main/java/org/novalegal/LegalDocSimplifierApplication.java — Spring Boot entry point (@EnableAsync)
- src/main/java/org/novalegal/services/impl/MeetingTranscriptionServiceImpl.java — GPU transcription + summarization orchestration
- src/main/resources/modal/modal_whisper_app.py — Modal GPU Whisper service (FastAPI)
- src/main/resources/application.properties — configuration binding to environment
- src/main/resources/templates/ — Thymeleaf templates (e.g., meeting-summarizer/*)
- docker-compose.yml — containerized local stack config

## Troubleshooting
- App can’t connect to DB:
  - Verify SPRING_DATASOURCE_URL/USERNAME/PASSWORD
  - Ensure PostgreSQL is running and accessible
- Large uploads fail:
  - Check spring.servlet.multipart.max-file-size and max-request-size (configured to 1GB)
- GCS errors:
  - Ensure GOOGLE_APPLICATION_CREDENTIALS points to a valid service account JSON with Storage access
  - Verify GCS_BUCKET_NAME exists and the service account has permissions
- Modal/Whisper errors:
  - Check that MODAL_WHISPER_TRANSCRIBER_URL is correct and reachable
  - Ensure TRANSCRIBE_API_KEY matches the Modal service’s expected secret
  - Verify the signed URL is valid and not expired; increase the URL TTL if jobs queue
  - Confirm the Modal function timeout (Python) is sufficient for your audio length
- Stripe webhook/requests failing:
  - Use correct STRIPE_API_KEY and STRIPE_WEBHOOK_SECRET
  - Ensure success/cancel URLs are reachable
- Email not sending:
  - Confirm SMTP settings and TLS (STARTTLS enabled)
- Debugging:
  - When running in Docker, ensure JDWP is not double-configured; bootRun block already avoids duplicate agents

## License
Add your chosen license here (e.g., Apache-2.0, MIT).
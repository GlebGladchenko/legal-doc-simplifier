# Legal Doc Simplifier

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
- Document parsing:
  - PDF parsing using Apache PDFBox
  - Word (.docx) parsing using Apache POI
- Web APIs and server-side rendering using Spring Web and Thymeleaf
- Persistence with Spring Data JPA and PostgreSQL (H2 for tests)
- Stripe payment integration
- Email sending (SMTP) via Spring Mail
- GCS-backed storage for uploads or processed artifacts
- Async processing with Spring’s @EnableAsync
- Configurable external transcription service endpoint for audio/video files

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
- src/main/resources/application.properties — configuration binding to environment
- src/main/resources/templates/ — Thymeleaf templates (e.g., blog/index.html)
- docker-compose.yml — containerized local stack config

## Troubleshooting
- App can’t connect to DB:
  - Verify SPRING_DATASOURCE_URL/USERNAME/PASSWORD
  - Ensure PostgreSQL is running and accessible
- Large uploads fail:
  - Check spring.servlet.multipart.max-file-size and max-request-size (configured to 1GB)
- GCS errors:
  - Ensure GOOGLE_APPLICATION_CREDENTIALS points to a valid service account JSON with storage access
  - Verify GCS_BUCKET_NAME exists and the service account has permissions
- Stripe webhook/requests failing:
  - Use correct STRIPE_API_KEY and STRIPE_WEBHOOK_SECRET
  - Ensure success/cancel URLs are reachable
- Email not sending:
  - Confirm SMTP settings and TLS (STARTTLS enabled)
- Debugging:
  - When running in Docker, ensure JDWP is not double-configured; bootRun block already avoids duplicate agents

## License
Add your chosen license here (e.g., Apache-2.0, MIT).
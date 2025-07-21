# 🎧 Whisper Transcriber GPU – Modal Serverless App

A serverless FastAPI application powered by [OpenAI Whisper](https://github.com/openai/whisper), running on Modal with GPU acceleration. It transcribes `.mp3` audio files from a **signed Google Cloud Storage (GCS) URL** using the Whisper model.

---

## ✅ Features

- 🔁 Transcribe `.mp3` files from signed GCS URLs
- 🚀 GPU-powered (A10G) for fast inference
- 🔐 API key-based authentication
- 🌐 FastAPI web server deployed with Modal's `@asgi_app()`
- 🐳 Lightweight, built on `debian-slim` + Python 3.10
- 🧠 Uses `whisper` model (`base`) for accurate transcriptions

---

## 🚀 Quickstart

2. Set up Modal
   •	Install the Modal CLI
   •	Log in:
   modal token new
3. Define Your Secret

You’ll need a Modal secret named whisper-auth-key with a single key-value pair:
•	TRANSCRIBE_API_KEY: your custom bearer token (e.g., abc123devsecret)
modal secret create whisper-auth-key
4. Deploy the Server
   modal deploy modal_whisper_app.py
   Once deployed, Modal will return a public endpoint URL for your FastAPI app.

🧠 How It Works

/transcribe-gcs POST

Transcribes an MP3 file from a signed URL (e.g., from Google Cloud Storage).

🔐 Authentication

Pass a Bearer token in the Authorization header that matches TRANSCRIBE_API_KEY.

📥 Request Body (JSON)
    {
        "signed_url": "https://storage.googleapis.com/your-bucket/audio.mp3?X-Goog-Signature=..."
    }

✅ Example Request
curl -X POST https://<modal-app-endpoint>/transcribe-gcs \
-H "Authorization: Bearer abc123devsecret" \
-H "Content-Type: application/json" \
-d '{"signed_url": "https://storage.googleapis.com/your-bucket/audio.mp3?..."}'

📝 Notes
•	Currently uses the base Whisper model. You can change this to tiny, small, or medium inside the transcribe_signed_url_mp3 function.
•	Only .mp3 files are supported. Ensure you convert other formats before uploading to GCS.
•	ffmpeg is included in the container image for future extensibility (e.g., format conversion).

⸻

🛡️ License

MIT

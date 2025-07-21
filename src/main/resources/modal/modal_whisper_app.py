import modal
import os
import tempfile
from fastapi import FastAPI, Request, HTTPException

# ✅ Define custom image and install dependencies
whisper_image = (
    modal.Image.debian_slim(python_version="3.10")  # Python 3.10 or 3.11
    .apt_install("git", "ffmpeg")
    .pip_install(
        "fastapi",
        "uvicorn",
        "torch",
        "python-multipart",
        "requests",  # ✅ here it is
        "git+https://github.com/openai/whisper.git"
    )
)

# ✅ Define Modal app and secret
app = modal.App(name="whisper-transcriber-gpu")
secrets = modal.Secret.from_name("whisper-auth-key")

# ✅ GPU Whisper function
@app.function(gpu="A10G", timeout=300, secrets=[secrets], image=whisper_image)
def transcribe_signed_url_mp3(signed_url: str):
    import whisper
    import requests  # ✅ also import here (inside Modal function)

    model = whisper.load_model("base")

    with tempfile.NamedTemporaryFile(suffix=".mp3", delete=False) as tmp_file:
        with requests.get(signed_url, stream=True) as r:
            r.raise_for_status()
            for chunk in r.iter_content(chunk_size=8192):
                tmp_file.write(chunk)
        tmp_file.flush()

        result = model.transcribe(tmp_file.name)
    return result

# ✅ FastAPI web app
web_app = FastAPI()

@web_app.post("/transcribe-gcs")
async def transcribe_from_signed_url(request: Request):
    expected_api_key = os.getenv("TRANSCRIBE_API_KEY", "dev-secret")
    auth_header = request.headers.get("Authorization")

    if not auth_header or not auth_header.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Missing or invalid Authorization header")

    token = auth_header.split("Bearer ")[1]
    if token != expected_api_key:
        raise HTTPException(status_code=403, detail="Invalid API key")

    data = await request.json()
    signed_url = data.get("signed_url")
    if not signed_url:
        raise HTTPException(status_code=400, detail="Missing signed_url")

    result = transcribe_signed_url_mp3.remote(signed_url)
    return result

# ✅ Mount FastAPI app
@app.function(secrets=[secrets], image=whisper_image)
@modal.asgi_app()
def fastapi_app():
    return web_app
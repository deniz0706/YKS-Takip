# YKS Takip AI Gateway

## Status

A runnable FastAPI gateway foundation now exists in this directory. It is not deployed and is not yet a public multi-user production service.

## Local setup

1. Install Python 3.12 or newer.
2. Copy `.env.example` to `.env`.
3. Set `NVIDIA_API_KEY` to the provider credential on the server only.
4. Set `YKS_CLIENT_TOKEN` to a long random secret.
5. Install dependencies and start the service:

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
set -a && . ./.env && set +a
uvicorn app.main:app --host 127.0.0.1 --port 8080
```

The health check is `GET /health`. The OpenAI-compatible endpoint is `POST /v1/chat/completions`.

## Android client configuration

Set the Android AI Base URL to the deployed gateway origin plus `/v1`, for example `https://your-domain.example/v1`. The bearer credential saved in the app must match `YKS_CLIENT_TOKEN`; it is not the NVIDIA provider key. The app's existing request path appends `/chat/completions`.

The server reads the provider credential from `NVIDIA_API_KEY` and forwards the model, messages, temperature, token limit, and streaming flag to the configured NVIDIA-compatible upstream.

## Security and deployment boundary

- Provider credentials are read from server environment variables and are never returned to the client.
- HTTPS is required for deployment; terminate TLS at a trusted reverse proxy or managed platform.
- The gateway uses constant-time comparison for its bearer token, validates request sizes, and has a basic in-memory request limiter.
- The current static client token is shared by a single deployment and can be extracted from an APK. **Do not expose this configuration as a public multi-user production service.**
- The in-memory rate limiter resets on restart and is not shared across replicas.
- Before public release, implement per-user/device JWT issuance, expiry and revocation, a shared rate-limit store (such as Redis), abuse monitoring, and a secret manager.
- Restrict outbound network access and configure trusted proxy headers deliberately. Do not enable permissive CORS by default.
- Never commit `.env`, provider keys, client tokens, signing keys, or production credentials.

## Validation

The GitHub workflow `.github/workflows/verify-ai-service.yml` installs the Python dependencies, runs pytest, and builds the Docker image. A green workflow verifies tests and image construction only; it does not deploy the service or prove connectivity to NVIDIA.

## Shared API contract

Request shape follows the OpenAI chat-completions format used by the Android client:

```json
{
  "model": "mistralai/mistral-large-4-1t-a52b",
  "temperature": 0.4,
  "max_tokens": 2048,
  "stream": true,
  "messages": [
    {"role": "system", "content": "YKS çalışma bağlamı"},
    {"role": "user", "content": "Netlerimi analiz et"}
  ]
}
```

Streaming responses are passed through as Server-Sent Events. Non-streaming responses are returned as JSON. Upstream provider failures are normalized so raw provider response bodies and headers are not exposed.

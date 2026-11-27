# YKS Takip 4.0 — Python AI Service Design

## Overview

Independent Python AI service that acts as a proxy between clients (Android, iOS, Web) and NVIDIA NIM / OpenAI-compatible LLM providers. All API keys and provider credentials live exclusively on the server — never on client devices.

## Architecture

```
Client (Android/iOS/Web)  ──HTTPS──►  Python AI Service  ──HTTPS──►  NVIDIA NIM
                                                              (or OpenAI-compatible)
```

## API Contract

### Base URL: `https://api.ykstakip.example.com/ai/v1`

#### POST /chat
Request:
```json
{
  "messages": [{"role": "user", "content": "Merhaba", "image_data_urls": []}],
  "max_tokens": 2048,
  "stream": true
}
```
Response (SSE stream or JSON):
```
data: {"delta": "Merhaba! Size nasıl yardımcı olabilir?", "finish_reason": null}
data: [DONE]
```

#### POST /context
Request:
```json
{
  "exams": [...],
  "study_days": [...],
  "learning_outcomes": [...],
  "study_plan": [...],
  "settings": {...}
}
```
Response:
```json
{"context": "Tarih: ...\nHedefler: ..."}
```

#### GET /health
Response: `{"status": "ok"}`

## Security

- API keys stored server-side (environment variables or secret manager)
- Client authenticates with app-level JWT or device token
- No provider credentials exposed to client
- Rate limiting per client

## Platform Support

- Android: Retrofit/OkHttp client calls `/ai/v1/chat`
- iOS: URLSession calls `/ai/v1/chat`
- Web: fetch() calls `/ai/v1/chat`

## Requirements

- Python 3.11+
- FastAPI or Flask
- httpx for upstream calls
- python-jose for JWT validation
- pydantic for request validation

## Deployment

- Docker container
- Gunicorn/uvicorn
- Behind HTTPS reverse proxy

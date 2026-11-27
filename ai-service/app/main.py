"""Small OpenAI-compatible NVIDIA proxy for YKS Takip.

This is a single-tenant deployment foundation. For public multi-user production,
replace the static bearer token and in-memory limiter with per-user JWT/device
credentials and a shared rate-limit store before exposing the service.
"""
from __future__ import annotations

import os
import secrets
import time
from collections import defaultdict, deque
from typing import Any, AsyncIterator

import httpx
from fastapi import Depends, FastAPI, HTTPException, Request, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, ConfigDict, Field
from starlette.responses import JSONResponse, StreamingResponse

app = FastAPI(
    title="YKS Takip AI Gateway",
    version="1.0.0",
    docs_url=None,
    redoc_url=None,
)
bearer = HTTPBearer(auto_error=False)
_request_times: dict[str, deque[float]] = defaultdict(deque)


class ChatMessage(BaseModel):
    model_config = ConfigDict(extra="forbid")
    role: str = Field(min_length=1, max_length=16)
    content: str | list[dict[str, Any]] = Field(max_length=16000)


class ChatRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")
    model: str | None = Field(default=None, max_length=160)
    messages: list[ChatMessage] = Field(min_length=1, max_length=32)
    temperature: float = Field(default=0.4, ge=0, le=2)
    max_tokens: int = Field(default=2048, ge=1, le=4096)
    stream: bool = False
    reasoning_effort: str | None = Field(default=None, max_length=16)


def _required_env(name: str) -> str:
    value = os.getenv(name, "").strip()
    if not value:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Sunucu yapılandırması eksik: {name}",
        )
    return value


def _authenticate(credentials: HTTPAuthorizationCredentials | None) -> str:
    expected = _required_env("YKS_CLIENT_TOKEN")
    supplied = credentials.credentials if credentials else ""
    if not supplied or not secrets.compare_digest(supplied, expected):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Geçersiz erişim anahtarı.",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return "single-tenant"


def _rate_limit(client_id: str) -> None:
    now = time.monotonic()
    window = 60.0
    limit = max(1, int(os.getenv("AI_MAX_REQUESTS_PER_MINUTE", "20")))
    events = _request_times[client_id]
    while events and now - events[0] >= window:
        events.popleft()
    if len(events) >= limit:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="İstek sınırına ulaşıldı. Bir dakika sonra tekrar deneyin.",
            headers={"Retry-After": "60"},
        )
    events.append(now)


@app.get("/health")
async def health() -> dict[str, str]:
    # Do not reveal credential/configuration state from an unauthenticated probe.
    return {"status": "ok"}


@app.post("/v1/chat/completions")
async def chat_completions(
    payload: ChatRequest,
    request: Request,
    _client_id: str = Depends(_authenticate),
    _rate_limit_result: None = Depends(lambda: None),
):
    _rate_limit(_client_id)
    api_key = _required_env("NVIDIA_API_KEY")
    base_url = os.getenv(
        "NVIDIA_BASE_URL", "https://integrate.api.nvidia.com/v1"
    ).rstrip("/")
    model = payload.model or os.getenv(
        "AI_DEFAULT_MODEL", "mistralai/mistral-large-4-1t-a52b"
    )
    timeout = float(os.getenv("AI_UPSTREAM_TIMEOUT_SECONDS", "75"))
    body = payload.model_dump(exclude_none=True)
    body["model"] = model

    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "Accept": "text/event-stream" if payload.stream else "application/json",
    }

    client = httpx.AsyncClient(timeout=httpx.Timeout(timeout, connect=15.0))
    try:
        upstream_request = client.build_request(
            "POST",
            f"{base_url}/chat/completions",
            json=body,
            headers=headers,
        )
        upstream = await client.send(upstream_request, stream=payload.stream)
    except httpx.TimeoutException:
        await client.aclose()
        raise HTTPException(status_code=504, detail="AI sağlayıcısı zaman aşımına uğradı.")
    except httpx.HTTPError:
        await client.aclose()
        raise HTTPException(status_code=502, detail="AI sağlayıcısına bağlanılamadı.")

    if upstream.status_code < 200 or upstream.status_code >= 300:
        raw = (await upstream.aread()).decode("utf-8", errors="replace")
        await upstream.aclose()
        await client.aclose()
        # Do not return upstream headers or credentials to the client.
        raise HTTPException(
            status_code=502,
            detail=f"AI sağlayıcısı isteği reddetti (HTTP {upstream.status_code}).",
        )

    if payload.stream:
        async def stream_body() -> AsyncIterator[bytes]:
            try:
                async for chunk in upstream.aiter_raw():
                    yield chunk
            finally:
                await upstream.aclose()
                await client.aclose()

        return StreamingResponse(
            stream_body(),
            media_type="text/event-stream",
            headers={
                "Cache-Control": "no-cache",
                "X-Accel-Buffering": "no",
            },
        )

    try:
        response_body = await upstream.aread()
        return JSONResponse(
            content=httpx.Response(
                status_code=upstream.status_code,
                content=response_body,
                headers=upstream.headers,
            ).json()
        )
    except (ValueError, httpx.DecodingError):
        raise HTTPException(status_code=502, detail="AI sağlayıcısından geçersiz JSON alındı.")
    finally:
        await upstream.aclose()
        await client.aclose()

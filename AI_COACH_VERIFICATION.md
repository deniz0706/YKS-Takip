# AI Coach — verified implementation status

## Android client

- OpenAI-compatible chat-completions request format.
- SSE response parsing with one JSON fallback for HTTP 400.
- HTTPS-only base URL validation.
- API credential encrypted with Android Keystore AES-GCM.
- Connection and read timeouts; coroutine cancellation disconnects the HTTP connection.
- Model and token limits are validated before sending.
- AI memory stores only explicit user-requested facts; it is not the conversation transcript.

## Python gateway foundation

Implemented under `ai-service/`:

- FastAPI `POST /v1/chat/completions` compatible with the Android client's path and payload.
- NVIDIA provider credential read only from server environment.
- Bearer-token gate using constant-time comparison.
- Request-count throttling and bounded request schema.
- SSE passthrough and non-streaming JSON response.
- Generic upstream errors; provider response headers and raw error bodies are not forwarded.
- Docker image and Python test workflow.
- `.env`, virtual environments, caches, and Python bytecode are ignored by Git.

## Important deployment boundary

The gateway is a **single-tenant deployment foundation**, not a finished public multi-user identity system.

- The client must use the gateway URL ending in `/v1` and the configured `YKS_CLIENT_TOKEN` as its bearer credential.
- `NVIDIA_API_KEY` stays on the server.
- The current static client token can be extracted from a distributed APK. Do not expose a public production service with a shared token. Before public launch, add per-user/device JWT issuance and revocation plus a shared rate-limit store (for example Redis).
- The in-memory rate limiter resets on process restart and is not shared across replicas.
- No cloud host, production domain, NVIDIA secret, or user authentication provider is configured by this repository.

## Still pending

- End-to-end Android-to-deployed-gateway test.
- AI ViewModel unit tests and chat UI tests.
- True token-by-token UI updates; the current client renders the completed response.
- Conversation transcript persistence/export.
- Per-user authentication, token revocation, billing/quotas, abuse monitoring, and distributed rate limiting.
- Production deployment and secret-manager integration.

Do not describe the AI gateway as production-ready until these items are completed and verified.

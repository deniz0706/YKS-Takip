# YKS Takip 4.0 — AI Coach Verification

## Implemented (Client Side)

### Lifecycle

| Feature | Status | Implementation |
|---------|--------|----------------|
| Draft editing | ✅ | `draft` state bound to `TextField`, `onDraftChange` updates ViewModel |
| Send message | ✅ | `onSend()` validates draft, adds user message, clears draft, sets `isSending` |
| Streaming responses | ✅ | `NvidiaAiService.chat()` reads SSE `data:` chunks, `[DONE]` terminator |
| Conversation history | ✅ | `uiState.messages` list, persisted via `AiMemoryRepository` |
| Loading state | ✅ | `isSending` / `isStreaming` flags show `CircularProgressIndicator` |
| Error state | ✅ | `error` field shown in red `PremiumErrorCard` with dismiss |
| Retry | ✅ | Non-streaming fallback on HTTP 400 from streaming attempt |
| Cancellation | ✅ | `Job.invokeOnCompletion` disconnects `HttpURLConnection` on coroutine cancellation |
| Clear error | ✅ | `clearError()` resets error state |
| Memory management | ✅ | `rememberFact()`, `forgetMemory()`, `refreshMemoryMessages()` |

### Security

| Feature | Status | Implementation |
|---------|--------|----------------|
| API key storage | ✅ | `AiSecretStore` — encrypted SharedPreferences on device |
| API key never in code | ✅ | Keys loaded at runtime from secure store |
| Base URL validation | ✅ | `require(base.startsWith("https://"))` |
| Model name validation | ✅ | `require(model.isNotBlank())` |
| Image validation | ✅ | Max 8 images per message, `data:image/` + `;base64,` format required |

### Network

| Feature | Status | Implementation |
|---------|--------|----------------|
| SSE streaming | ✅ | `Accept: text/event-stream`, line-by-line `data:` parsing |
| JSON fallback | ✅ | Retries with `Accept: application/json` on HTTP 400 |
| Timeout | ✅ | 15s connect, 60s read |
| Reasoning effort | ✅ | Model-specific `reasoning_effort` parameter |

## Requires Independent Python Backend

The Android client communicates with an OpenAI-compatible endpoint at `AppSettings.getAiBaseUrl(context)`. The following is **NOT** implemented in this repository:

| Component | Status | Required |
|-----------|--------|----------|
| Python AI service | ❌ | `ai-service/PYTHON_AI_SERVICE_DESIGN.md` documents the FastAPI proxy design |
| NVIDIA NIM proxy | ❌ | Server-side proxy forwarding to NVIDIA NIM / OpenAI-compatible LLM |
| API key management (server) | ❌ | Server stores provider credentials; client never sees them |
| JWT / device auth | ❌ | Client authenticates with app-level JWT or device token |
| Rate limiting | ❌ | Per-client rate limiting on server |
| Docker deployment | ❌ | Containerized Python service behind HTTPS reverse proxy |

### Client → Server Contract

```
POST {baseUrl}/chat/completions
Headers: Authorization: Bearer {apiKey}, Content-Type: application/json
Body: { model, temperature, max_tokens, stream, messages: [{role, content|[{type,text|image_url}]}] }
Response (SSE): data: {choices:[{delta:{content}}]} ... data: [DONE]
```

### Setup Required for Production

1. Deploy the Python AI service per `ai-service/PYTHON_AI_SERVICE_DESIGN.md`
2. Configure `AI_BASE_URL` and `AI_MODEL` in Settings screen
3. Store NVIDIA API key via Settings (stored client-side, sent to your proxy)
4. The proxy forwards to NVIDIA NIM with server-side credentials

## Test Coverage

| Test Area | Status |
|-----------|--------|
| Unit tests for ViewModel | Pending (no unit test file for AiCoachViewModel) |
| Integration tests (AI service) | Pending (requires mock server) |
| UI tests for chat flow | Pending |

## Known Limitations

1. No actual streaming UI updates mid-response — the full response is rendered when received. True token-by-token streaming would require collecting deltas incrementally in the ViewModel.
2. No message retry on transient network failure beyond the single JSON fallback.
3. No conversation export/share.
4. iOS (SwiftUI) and Web (React) clients would need their own AI client implementations calling the same Python backend.
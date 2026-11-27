import os
from collections import deque

import pytest
from fastapi import HTTPException
from fastapi.security import HTTPAuthorizationCredentials

from app.main import _authenticate, _rate_limit, _request_times


def test_auth_rejects_missing_or_invalid_token(monkeypatch):
    monkeypatch.setenv("YKS_CLIENT_TOKEN", "expected-secret")
    with pytest.raises(HTTPException) as missing:
        _authenticate(None)
    assert missing.value.status_code == 401

    wrong = HTTPAuthorizationCredentials(scheme="Bearer", credentials="wrong")
    with pytest.raises(HTTPException) as invalid:
        _authenticate(wrong)
    assert invalid.value.status_code == 401


def test_auth_accepts_configured_bearer_token(monkeypatch):
    monkeypatch.setenv("YKS_CLIENT_TOKEN", "expected-secret")
    credentials = HTTPAuthorizationCredentials(
        scheme="Bearer", credentials="expected-secret"
    )
    assert _authenticate(credentials) == "single-tenant"


def test_rate_limit_blocks_requests_after_configured_limit(monkeypatch):
    monkeypatch.setenv("AI_MAX_REQUESTS_PER_MINUTE", "2")
    _request_times.clear()

    _rate_limit("test-client")
    _rate_limit("test-client")
    with pytest.raises(HTTPException) as limited:
        _rate_limit("test-client")

    assert limited.value.status_code == 429
    assert limited.value.headers["Retry-After"] == "60"
    _request_times.clear()


def test_rate_limit_isolated_between_clients(monkeypatch):
    monkeypatch.setenv("AI_MAX_REQUESTS_PER_MINUTE", "1")
    _request_times.clear()

    _rate_limit("client-a")
    _rate_limit("client-b")
    with pytest.raises(HTTPException):
        _rate_limit("client-a")
    _request_times.clear()

#!/usr/bin/env bash
set -euo pipefail

# Shared Kotlin code must not depend on platform or JVM-only APIs.
# Keep this check intentionally strict: use expect/actual or platform adapters
# instead of importing these packages from commonMain.
forbidden_pattern='(^|[^[:alnum:]_])(android|java|org\.json)\.'

if grep -RInE --include='*.kt' "$forbidden_pattern" \
  core/domain/src/commonMain \
  core/database/src/commonMain; then
  echo "::error::Platform-specific API reference found in commonMain. Move it to a platform source set or introduce a shared abstraction."
  exit 1
fi

echo "commonMain platform-boundary check passed."

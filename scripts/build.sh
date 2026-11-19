#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
if command -v mvn >/dev/null 2>&1; then MVN="$(command -v mvn)"
elif [[ -x "$ROOT/.tools/apache-maven-3.9.9/bin/mvn" ]]; then MVN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"
else printf '[X504] Ejecuta primero: bash scripts/bootstrap.sh\n' >&2; exit 1; fi
"$MVN" --batch-mode --no-transfer-progress clean verify

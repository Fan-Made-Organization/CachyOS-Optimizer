#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
if [[ -x "$ROOT/.tools/temurin-jdk-21/bin/java" ]]; then
  export JAVA_HOME="$ROOT/.tools/temurin-jdk-21"
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if command -v mvn >/dev/null 2>&1; then MVN="$(command -v mvn)"
elif [[ -x "$ROOT/.tools/apache-maven-3.9.9/bin/mvn" ]]; then MVN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"
else printf '[CachyOS Optimizer] Ejecuta primero: bash scripts/bootstrap.sh\n' >&2; exit 1; fi
"$MVN" -Dmaven.repo.local="$ROOT/.cache/maven-repository" --batch-mode --no-transfer-progress clean verify

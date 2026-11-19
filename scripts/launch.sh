#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$ROOT/target/cachyos-optimizer-0.1.0-SNAPSHOT.jar"
if [[ ! -f "$JAR" ]]; then bash "$ROOT/scripts/package.sh"; fi
JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if [[ ! -x "$JAVA_BIN" ]]; then JAVA_BIN="$(command -v java || true)"; fi
[[ -n "$JAVA_BIN" && -x "$JAVA_BIN" ]] || { printf '[X504] No se encontró Java. Instala JDK 21.\n' >&2; exit 1; }
exec "$JAVA_BIN" -jar "$JAR" "$@"

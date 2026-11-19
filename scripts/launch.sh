#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
JAR="$ROOT/target/cachyos-optimizer-0.1.0-SNAPSHOT.jar"
LOCAL_JAVA="$ROOT/.tools/temurin-jdk-21/bin/java"
LOCAL_MAVEN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"

say() { printf '[X504] %s\\n' "$*"; }

java_is_21() {
  local binary="$1" version
  [[ -x "$binary" ]] || return 1
  version="$("$binary" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\\1/p')"
  [[ "$version" == "21" ]]
}

# Prefer the project-local JDK so the user does not need to configure JAVA_HOME.
if [[ -x "$LOCAL_JAVA" ]]; then
  export JAVA_HOME="$ROOT/.tools/temurin-jdk-21"
  export PATH="$JAVA_HOME/bin:$PATH"
elif command -v java >/dev/null 2>&1 && java_is_21 "$(command -v java)"; then
  :
else
  say "Preparando Java 21 y Maven automáticamente dentro del repositorio..."
  bash "$ROOT/scripts/bootstrap.sh"
  if [[ -x "$LOCAL_JAVA" ]]; then
    export JAVA_HOME="$ROOT/.tools/temurin-jdk-21"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if ! command -v mvn >/dev/null 2>&1 && [[ ! -x "$LOCAL_MAVEN" ]]; then
  say "Maven no está disponible; ejecutando la preparación automática..."
  bash "$ROOT/scripts/bootstrap.sh"
fi

if [[ ! -f "$JAR" ]] || find "$ROOT/src/main" "$ROOT/pom.xml" -type f -newer "$JAR" -print -quit | grep -q .; then
  say "Compilando o actualizando X504 CachyOS Optimizer..."
  bash "$ROOT/scripts/package.sh"
fi

if [[ -x "$LOCAL_JAVA" ]]; then
  JAVA_BIN="$LOCAL_JAVA"
elif [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
else
  JAVA_BIN="$(command -v java || true)"
fi

[[ -n "$JAVA_BIN" && -x "$JAVA_BIN" ]] || {
  printf '[X504] ERROR: no se encontró Java 21. Ejecuta: bash scripts/bootstrap.sh\\n' >&2
  exit 1
}

exec "$JAVA_BIN" -jar "$JAR" "$@"

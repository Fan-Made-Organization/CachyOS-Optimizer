#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$ROOT/.tools"
CACHE_DIR="$ROOT/.cache/maven-repository"
MAVEN_VERSION="3.9.9"
MAVEN_HOME="$TOOLS_DIR/apache-maven-$MAVEN_VERSION"

say() { printf '[X504] %s\n' "$*"; }
fail() { printf '[X504] ERROR: %s\n' "$*" >&2; exit 1; }

command -v curl >/dev/null 2>&1 || fail "Se necesita curl para descargar Maven."
command -v tar >/dev/null 2>&1 || fail "Se necesita tar para extraer Maven."
command -v sha512sum >/dev/null 2>&1 || fail "Se necesita sha512sum para verificar la descarga."

JAVA_BIN="${JAVA_HOME:+$JAVA_HOME/bin/}java"
if ! command -v "$JAVA_BIN" >/dev/null 2>&1; then
    JAVA_BIN="$(command -v java || true)"
fi
[[ -n "$JAVA_BIN" && -x "$JAVA_BIN" ]] || fail "No se encontró Java. Instala un JDK 21 y vuelve a ejecutar este script."
JAVA_VERSION="$("$JAVA_BIN" -version 2>&1 | head -n 1)"
JAVA_MAJOR="$(printf '%s' "$JAVA_VERSION" | sed -E 's/.*version "([0-9]+).*/\1/')"
[[ "$JAVA_MAJOR" == "21" ]] || fail "Se requiere Java 21. Detectado: $JAVA_VERSION"

mkdir -p "$TOOLS_DIR" "$CACHE_DIR"

if command -v mvn >/dev/null 2>&1; then
    MVN_BIN="$(command -v mvn)"
elif [[ -x "$MAVEN_HOME/bin/mvn" ]]; then
    MVN_BIN="$MAVEN_HOME/bin/mvn"
else
    say "Maven no está instalado. Descargando Maven $MAVEN_VERSION dentro de .tools/ ..."
    ARCHIVE="$TOOLS_DIR/apache-maven-$MAVEN_VERSION-bin.tar.gz"
    CHECKSUM="$ARCHIVE.sha512"
    BASE_URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries"
    curl --fail --location --retry 3 "$BASE_URL/apache-maven-$MAVEN_VERSION-bin.tar.gz" -o "$ARCHIVE"
    curl --fail --location --retry 3 "$BASE_URL/apache-maven-$MAVEN_VERSION-bin.tar.gz.sha512" -o "$CHECKSUM"
    EXPECTED="$(awk '{print $1}' "$CHECKSUM")"
    ACTUAL="$(sha512sum "$ARCHIVE" | awk '{print $1}')"
    [[ "$EXPECTED" == "$ACTUAL" ]] || fail "La verificación SHA-512 de Maven no coincide."
    tar -xzf "$ARCHIVE" -C "$TOOLS_DIR"
    rm -f "$ARCHIVE" "$CHECKSUM"
    MVN_BIN="$MAVEN_HOME/bin/mvn"
fi

say "Java: $JAVA_VERSION"
say "Maven: $("$MVN_BIN" -version | head -n 1)"
say "Las dependencias Maven se descargarán en $CACHE_DIR (dentro del repositorio y excluidas de Git)."
cd "$ROOT"
"$MVN_BIN" -Dmaven.repo.local="$CACHE_DIR" --batch-mode --no-transfer-progress clean verify
say "Compilación y pruebas finalizadas correctamente."

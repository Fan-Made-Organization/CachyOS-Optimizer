#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="$ROOT/.tools"
CACHE_DIR="$ROOT/.cache/maven-repository"
MAVEN_VERSION="3.9.9"
MAVEN_HOME="$TOOLS_DIR/apache-maven-$MAVEN_VERSION"
JDK_HOME="$TOOLS_DIR/temurin-jdk-21"

say() { printf '[X504] %s\n' "$*"; }
fail() { printf '[X504] ERROR: %s\n' "$*" >&2; exit 1; }

for tool in curl tar sha512sum sha256sum awk sed python3 uname; do
  command -v "$tool" >/dev/null 2>&1 || fail "Falta la herramienta base requerida: $tool"
done

java_major() {
  local binary="$1" version
  version="$("$binary" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')"
  [[ "$version" =~ ^[0-9]+$ ]] || return 1
  printf '%s' "$version"
}

JAVA_BIN=""
if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
  JAVA_BIN="$(command -v java)"
fi

JAVA_MAJOR=""
if [[ -n "$JAVA_BIN" ]]; then JAVA_MAJOR="$(java_major "$JAVA_BIN" || true)"; fi

if [[ "$JAVA_MAJOR" != "21" ]]; then
  [[ "$(uname -s)" == "Linux" ]] || fail "El JDK 21 local automático está preparado para Linux."
  case "$(uname -m)" in
    x86_64) ADOPTIUM_ARCH="x64" ;;
    aarch64|arm64) ADOPTIUM_ARCH="aarch64" ;;
    *) fail "Arquitectura no compatible con la descarga automática de JDK: $(uname -m)" ;;
  esac

  mkdir -p "$TOOLS_DIR"
  say "No se encontró JDK 21. Descargando Temurin JDK 21 en .tools/ ..."
  ASSET_JSON="$TOOLS_DIR/temurin-jdk21-assets.json"
  curl --fail --location --retry 3 \
    "https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=$ADOPTIUM_ARCH&image_type=jdk&os=linux&vendor=eclipse" \
    -o "$ASSET_JSON"

  read -r JDK_URL JDK_SHA256 < <(python3 - "$ASSET_JSON" <<'PY'
import json
import sys
with open(sys.argv[1], encoding="utf-8") as source:
    assets = json.load(source)
if not assets:
    raise SystemExit("Adoptium no publicó un JDK 21 para esta arquitectura.")
package = assets[0]["binary"]["package"]
print(package["link"], package["checksum"])
PY
  )
  [[ "$JDK_URL" == https://* && "$JDK_SHA256" =~ ^[a-fA-F0-9]{64}$ ]] \
    || fail "La API de Adoptium devolvió metadatos de descarga no válidos."

  JDK_ARCHIVE="$TOOLS_DIR/temurin-jdk21.tar.gz"
  curl --fail --location --retry 3 "$JDK_URL" -o "$JDK_ARCHIVE"
  printf '%s  %s\n' "$JDK_SHA256" "$JDK_ARCHIVE" | sha256sum --check --status \
    || fail "La verificación SHA-256 del JDK 21 no coincide."

  rm -rf -- "$JDK_HOME"
  mkdir -p "$JDK_HOME"
  tar -xzf "$JDK_ARCHIVE" --strip-components=1 -C "$JDK_HOME"
  rm -f "$JDK_ARCHIVE" "$ASSET_JSON"
  JAVA_BIN="$JDK_HOME/bin/java"
  [[ -x "$JAVA_BIN" && "$(java_major "$JAVA_BIN")" == "21" ]] \
    || fail "El JDK descargado no superó la comprobación de versión."
  export JAVA_HOME="$JDK_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
  say "JDK 21 instalado localmente en $JDK_HOME"
else
  say "JDK 21 detectado: $JAVA_BIN"
fi

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

say "Java: $("$JAVA_BIN" -version 2>&1 | sed -n '1p')"
say "Maven: $("$MVN_BIN" -version | sed -n '1p')"
say "Dependencias Maven locales: $CACHE_DIR"
cd "$ROOT"
"$MVN_BIN" -Dmaven.repo.local="$CACHE_DIR" --batch-mode --no-transfer-progress clean verify
say "Compilación y pruebas finalizadas correctamente."

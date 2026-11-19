#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
if [[ "$(uname -s)" != "Linux" ]]; then
  printf '[Linux Optimizer] Este proyecto solo es compatible con Linux. Windows y macOS no están soportados.\n' >&2
  exit 1
fi
JAR="$ROOT/target/linux-optimizer-0.1.0-SNAPSHOT.jar"
PENDING_UPDATE="${XDG_DATA_HOME:-$HOME/.local/share}/linux-optimizer/pending-update.jar"
LOCAL_JAVA="$ROOT/.tools/temurin-jdk-21/bin/java"
LOCAL_MAVEN="$ROOT/.tools/apache-maven-3.9.9/bin/mvn"
CONFIG_DIR="${XDG_CONFIG_HOME:-$HOME/.config}/linux-optimizer"
LANGUAGE_FILE="$CONFIG_DIR/language"

say() { printf '[Linux Optimizer] %s\n' "$*"; }

choose_language() {
  local selected=""
  if [[ -r "$LANGUAGE_FILE" ]]; then
    selected="$(<"$LANGUAGE_FILE")"
  elif [[ -r "${XDG_CONFIG_HOME:-$HOME/.config}/cachyos-optimizer/language" ]]; then
    selected="$(<"${XDG_CONFIG_HOME:-$HOME/.config}/cachyos-optimizer/language")"
  fi
  if [[ ! "$selected" =~ ^(es|en|pt_BR)$ ]]; then
    printf '\n==============================================\n'
    printf '       Linux Optimizer - Idioma / Language\n'
    printf '==============================================\n'
    printf '1) Español\n2) English\n3) Português (Brasil)\n'
    while true; do
      printf 'Selecciona / Choose [1-3]: '
      IFS= read -r choice || choice=1
      case "$choice" in
        1) selected="es"; break ;;
        2) selected="en"; break ;;
        3) selected="pt_BR"; break ;;
        *) printf 'Opción no válida / Invalid option.\n' ;;
      esac
    done
    mkdir -p "$CONFIG_DIR"
    printf '%s\n' "$selected" > "$LANGUAGE_FILE"
    chmod 600 "$LANGUAGE_FILE"
  fi
  export LINUX_OPTIMIZER_LANGUAGE="$selected"
  export CACHYOS_OPTIMIZER_LANGUAGE="$selected" # Compatibilidad con versiones anteriores
  case "$selected" in
    es) say "Idioma seleccionado: Español." ;;
    en) say "Selected language: English." ;;
    pt_BR) say "Idioma selecionado: Português (Brasil)." ;;
  esac
}

java_is_21() {
  local binary="$1" version
  [[ -x "$binary" ]] || return 1
  version="$("$binary" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')"
  [[ "$version" == "21" ]]
}

# La selección de idioma es el primer paso del lanzador.
choose_language

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

if [[ -f "$PENDING_UPDATE" ]]; then
  say "Instalando actualización verificada de GitHub..."
  mkdir -p "$(dirname "$JAR")"
  install -m 0644 "$PENDING_UPDATE" "$JAR.new"
  mv -f "$JAR.new" "$JAR"
  rm -f "$PENDING_UPDATE"
  say "Actualización aplicada correctamente."
fi

if [[ ! -f "$JAR" ]] || find "$ROOT/src/main" "$ROOT/pom.xml" -type f -newer "$JAR" -print -quit | grep -q .; then
  say "Compilando o actualizando Linux Optimizer..."
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
  printf '[Linux Optimizer] ERROR: no se encontró Java 21. Ejecuta: bash scripts/bootstrap.sh\n' >&2
  exit 1
}

# En el primer inicio ofrece el paquete de utilidades; nunca instala en silencio.
FIRST_RUN_FILE="${XDG_STATE_HOME:-$HOME/.local/state}/linux-optimizer/first-run-tools-check"
LEGACY_FIRST_RUN_FILE="${XDG_STATE_HOME:-$HOME/.local/state}/cachyos-optimizer/first-run-tools-check"
if [[ ! -e "$FIRST_RUN_FILE" && ! -e "$LEGACY_FIRST_RUN_FILE" ]] && { grep -qiE '^(ID|ID_LIKE)=.*(arch|cachyos|manjaro|debian|ubuntu|linuxmint|fedora|rhel|centos|rocky|almalinux|opensuse|suse)' /etc/os-release 2>/dev/null; }; then
  printf '\n'
  say "Primera ejecución: ¿quieres abrir el instalador de herramientas recomendadas?"
  say "Puedes aceptar o saltarlo; tu gestor de paquetes solicitará su propia confirmación antes de instalar."
  printf '¿Abrir instalador ahora? [s/N]: '
  IFS= read -r install_choice || install_choice="n"
  mkdir -p "$(dirname "$FIRST_RUN_FILE")"
  if [[ "$install_choice" =~ ^[sS]$ ]]; then
    "$JAVA_BIN" -Duser.language="${CACHYOS_OPTIMIZER_LANGUAGE%%_*}" -Duser.country="${CACHYOS_OPTIMIZER_LANGUAGE#*_}" -jar "$JAR" --install-tools
  else
    say "Instalación inicial omitida. Puedes ejecutarla después desde el menú."
  fi
  : > "$FIRST_RUN_FILE"
fi
exec "$JAVA_BIN" -Duser.language="${CACHYOS_OPTIMIZER_LANGUAGE%%_*}" -Duser.country="${CACHYOS_OPTIMIZER_LANGUAGE#*_}" -jar "$JAR" "$@"

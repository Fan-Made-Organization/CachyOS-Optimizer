#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd -- "$(dirname -- "\${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="\${XDG_DATA_HOME:-$HOME/.local/share}/linux-optimizer/app"
BIN_DIR="$HOME/.local/bin"
ICON_DIR="\${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/scalable/apps"
DESKTOP_DIR="\${XDG_DATA_HOME:-$HOME/.local/share}/applications"
JAR="$ROOT/target/linux-optimizer-0.1.0-SNAPSHOT.jar"
printf '\n Linux Optimizer · Instalador de escritorio\n'
if [[ ! -f "$JAR" ]] || find "$ROOT/src/main" "$ROOT/pom.xml" -type f -newer "$JAR" -print -quit | grep -q .; then
  bash "$ROOT/scripts/package.sh"
fi
mkdir -p "$APP_DIR" "$BIN_DIR" "$ICON_DIR" "$DESKTOP_DIR"
install -m 0644 "$JAR" "$APP_DIR/linux-optimizer.jar"
install -m 0644 "$ROOT/assets/linux-optimizer.svg" "$ICON_DIR/linux-optimizer.svg"
cat > "$BIN_DIR/linux-optimizer" <<'LAUNCHER'
#!/usr/bin/env bash
set -Eeuo pipefail
APP_DIR="\${XDG_DATA_HOME:-$HOME/.local/share}/linux-optimizer/app"
JAR="$APP_DIR/linux-optimizer.jar"
PENDING="\${XDG_DATA_HOME:-$HOME/.local/share}/linux-optimizer/pending-update.jar"
if [[ -f "$PENDING" ]]; then
  install -m 0644 "$PENDING" "$JAR.new"
  mv -f "$JAR.new" "$JAR"
  rm -f "$PENDING"
fi
JAVA_BIN=""
if [[ -x "$APP_DIR/jre/bin/java" ]]; then JAVA_BIN="$APP_DIR/jre/bin/java"
elif command -v java >/dev/null 2>&1; then JAVA_BIN="$(command -v java)"
fi
if [[ -z "$JAVA_BIN" ]]; then
  notify-send "Linux Optimizer" "Necesitas Java 21 para ejecutar la aplicación." 2>/dev/null || true
  echo "Linux Optimizer requiere Java 21. Instálalo desde tu distribución." >&2
  exit 1
fi
VERSION="$("$JAVA_BIN" -version 2>&1 | sed -nE '1s/.*version "([0-9]+).*/\1/p')"
if [[ "$VERSION" != "21" ]]; then
  echo "Linux Optimizer requiere Java 21; detectado: \${VERSION:-desconocido}." >&2
  exit 1
fi
exec "$JAVA_BIN" -Duser.language=es -Duser.country=HN -jar "$JAR" --gui "$@"
LAUNCHER
chmod 0755 "$BIN_DIR/linux-optimizer"
cat > "$DESKTOP_DIR/linux-optimizer.desktop" <<DESKTOP
[Desktop Entry]
Type=Application
Name=Linux Optimizer
Comment=Centro de control y monitorización de Linux
Exec=$BIN_DIR/linux-optimizer
Icon=linux-optimizer
Terminal=false
Categories=System;Monitor;Utility;
StartupNotify=true
DESKTOP
chmod 0644 "$DESKTOP_DIR/linux-optimizer.desktop"
if command -v update-desktop-database >/dev/null 2>&1; then update-desktop-database "$DESKTOP_DIR" >/dev/null 2>&1 || true; fi
printf '\nInstalación completada.\nBusca “Linux Optimizer” en el lanzador de aplicaciones.\n'
printf 'Comando: %s/linux-optimizer\n' "$BIN_DIR"

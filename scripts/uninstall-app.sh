#!/usr/bin/env bash
set -Eeuo pipefail
rm -f "$HOME/.local/bin/linux-optimizer"
rm -f "\${XDG_DATA_HOME:-$HOME/.local/share}/applications/linux-optimizer.desktop"
rm -f "\${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/scalable/apps/linux-optimizer.svg"
rm -rf "\${XDG_DATA_HOME:-$HOME/.local/share}/linux-optimizer/app"
printf 'Linux Optimizer se ha desinstalado. Se conservan configuración, registros y actualizaciones pendientes.\n'

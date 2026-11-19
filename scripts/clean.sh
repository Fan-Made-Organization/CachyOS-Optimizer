#!/usr/bin/env bash
set -Eeuo pipefail
ROOT="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET="$ROOT/target"
if [[ -d "$TARGET" && "$TARGET" == "$ROOT/target" ]]; then
  rm -rf -- "$TARGET"
  printf '[X504] Archivos generados eliminados: target/\n'
else
  printf '[X504] No hay archivos generados que limpiar.\n'
fi

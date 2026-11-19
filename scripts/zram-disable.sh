#!/usr/bin/env bash
set -euo pipefail
if [[ ${EUID} -ne 0 ]]; then echo 'Ejecuta este script mediante sudo.' >&2; exit 1; fi
config='/etc/systemd/zram-generator.conf'
state='/var/lib/cachyos-optimizer'
marker="$state/zram-managed"
expected=$(printf '[zram0]\nzram-size = min(ram / 2, 4096)\ncompression-algorithm = zstd\nswap-priority = 100')
if [[ ! -f "$marker" ]]; then echo 'No existe una configuración zram administrada por CachyOS Optimizer.' >&2; exit 2; fi
if [[ ! -f "$config" ]] || [[ "$(cat "$config")" != "$expected" ]]; then
  echo 'La configuración cambió desde que fue creada. No se eliminará para evitar borrar cambios del usuario.' >&2
  exit 3
fi
if systemctl is-active --quiet dev-zram0.swap; then systemctl stop dev-zram0.swap; fi
rm -f "$config" "$marker"
systemctl daemon-reload
echo 'Configuración zram administrada eliminada. El cambio es reversible; otros ajustes de swap no se modificaron.'
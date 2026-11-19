#!/usr/bin/env bash
set -euo pipefail
if [[ ${EUID} -ne 0 ]]; then echo 'Ejecuta este script mediante sudo.' >&2; exit 1; fi
command -v systemctl >/dev/null || { echo 'systemctl no está disponible.' >&2; exit 1; }
command -v zram-generator >/dev/null 2>&1 || true
config='/etc/systemd/zram-generator.conf'
state='/var/lib/cachyos-optimizer'
marker="$state/zram-managed"
if [[ -e "$marker" ]]; then echo 'CachyOS Optimizer ya administra esta configuración zram.'; exit 0; fi
if [[ -e "$config" ]]; then echo 'Ya existe zram-generator.conf. No se sobrescribirá una configuración existente.' >&2; exit 2; fi
mkdir -p "$state"
tmp=$(mktemp)
trap 'rm -f "$tmp"' EXIT
cat > "$tmp" <<'EOF'
[zram0]
zram-size = min(ram / 2, 4096)
compression-algorithm = zstd
swap-priority = 100
EOF
install -o root -g root -m 0644 "$tmp" "$config"
if ! systemctl daemon-reload || ! systemctl start dev-zram0.swap; then
  systemctl stop dev-zram0.swap >/dev/null 2>&1 || true
  rm -f "$config"
  systemctl daemon-reload || true
  echo 'No se pudo activar zram; se revirtió el archivo de configuración.' >&2
  exit 1
fi
printf '%s\n' 'CachyOS Optimizer managed zram config v1' > "$marker"
echo 'zram se configuró y activó. Tamaño objetivo: hasta la mitad de la RAM, con máximo de 4 GiB.'
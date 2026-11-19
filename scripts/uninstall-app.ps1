$ErrorActionPreference = "Stop"
$AppDir = Join-Path $env:LOCALAPPDATA "Programs\Linux Optimizer"
$Shortcut = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\Linux Optimizer.lnk"
Remove-Item -Force -ErrorAction SilentlyContinue $Shortcut
Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $AppDir
Write-Host "Linux Optimizer desinstalado. Se conservan los datos de usuario y las actualizaciones pendientes."

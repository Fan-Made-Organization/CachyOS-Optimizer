$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Jar = Join-Path $Root "target\linux-optimizer-0.1.0-SNAPSHOT.jar"
if (-not (Test-Path $Jar)) {
    throw "No se encontró el JAR. Instala Java 21 y Maven, luego ejecuta: mvn clean package"
}
$AppDir = Join-Path $env:LOCALAPPDATA "Programs\Linux Optimizer"
$StartMenu = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs"
New-Item -ItemType Directory -Force -Path $AppDir, $StartMenu | Out-Null
Copy-Item -Force $Jar (Join-Path $AppDir "linux-optimizer.jar")
$Launcher = Join-Path $AppDir "Linux-Optimizer.cmd"
@'
@echo off
setlocal
set "APP_DIR=%LOCALAPPDATA%\Programs\Linux Optimizer"
set "JAR=%APP_DIR%\linux-optimizer.jar"
set "PENDING=%USERPROFILE%\.local\share\linux-optimizer\pending-update.jar"
if exist "%PENDING%" (
  copy /Y "%PENDING%" "%JAR%.new" >nul
  if errorlevel 1 exit /b 1
  move /Y "%JAR%.new" "%JAR%" >nul
  del "%PENDING%"
)
where java >nul 2>nul
if errorlevel 1 (
  echo Linux Optimizer requiere Java 21. Instala Temurin u OpenJDK 21.
  pause
  exit /b 1
)
java -version 2>&1 | findstr /R /C:"version ""21" >nul
if errorlevel 1 (
  echo Se requiere Java 21.
  pause
  exit /b 1
)
java -Duser.language=es -Duser.country=HN -jar "%JAR%" --gui
'@ | Set-Content -Encoding ASCII $Launcher
$Shell = New-Object -ComObject WScript.Shell
$Shortcut = $Shell.CreateShortcut((Join-Path $StartMenu "Linux Optimizer.lnk"))
$Shortcut.TargetPath = $Launcher
$Shortcut.WorkingDirectory = $AppDir
$Shortcut.Description = "Linux Optimizer - monitor y diagnóstico multiplataforma"
$Shortcut.Save()
Write-Host "Linux Optimizer instalado. Abre Inicio y busca 'Linux Optimizer'."

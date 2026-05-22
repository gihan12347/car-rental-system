@echo off
setlocal enabledelayedexpansion
mkdir "gradle\wrapper" 2>nul
set "OUT=gradle\wrapper\gradle-wrapper.jar"
echo Downloading Gradle Wrapper jar to %OUT% ...

where curl >nul 2>&1
if %ERRORLEVEL% equ 0 (
  curl -fL -o "%OUT%" "https://github.com/gradle/gradle/raw/v7.6.4/gradle/wrapper/gradle-wrapper.jar"
  if !ERRORLEVEL! neq 0 goto err
  echo Done.
  exit /b 0
)

where powershell >nul 2>&1
if %ERRORLEVEL% equ 0 (
  powershell -NoProfile -Command "Invoke-WebRequest -Uri 'https://github.com/gradle/gradle/raw/v7.6.4/gradle/wrapper/gradle-wrapper.jar' -OutFile '%OUT%' -UseBasicParsing"
  if !ERRORLEVEL! neq 0 goto err
  echo Done.
  exit /b 0
)

:err
echo Could not download. Install Gradle and run: gradle wrapper --gradle-version 7.6.4
exit /b 1

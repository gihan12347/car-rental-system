@echo off
setlocal
cd /d "%~dp0"

REM Optional: set MySQL credentials if not using defaults (root + empty password)
REM set MYSQL_USER=root
REM set MYSQL_PASSWORD=secret

echo.
echo  FleetDesk — starting Spring Boot (this window must stay open)
echo  Open http://localhost:8080 in your browser after the server starts.
echo.

start "FleetDesk server" cmd /k "cd /d "%~dp0" && gradlew.bat bootRun"

echo Waiting for the app to come up (about 15 seconds)...
timeout /t 15 /nobreak >nul

start "" "http://localhost:8080"

echo.
echo  If the browser shows an error, wait a few seconds and refresh.
echo  Close the "FleetDesk server" window to stop the application.
echo.
pause

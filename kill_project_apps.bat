@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PORTS= 3000 8081 8082 8083 8084 8085 8086 8087 8088 8089 9000"
set "TMP_FILE=%TEMP%\kill_project_apps_pids_%RANDOM%.tmp"
set "DRY_RUN=0"

if /I "%~1"=="DRYRUN" set "DRY_RUN=1"

echo [INFO] Target ports: %PORTS%
if "%DRY_RUN%"=="1" echo [INFO] Dry-run enabled. No process will be killed.

del /q "%TMP_FILE%" >nul 2>&1

for %%A in (%PORTS%) do (
    for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%%A " ') do (
        if not "%%P"=="0" if not "%%P"=="4" echo %%P>>"%TMP_FILE%"
    )
)

if not exist "%TMP_FILE%" (
    echo [INFO] No matching process found.
    exit /b 0
)

set "FOUND=0"
for /f %%P in ('sort "%TMP_FILE%" /unique') do (
    call :handle_pid %%P
)

del /q "%TMP_FILE%" >nul 2>&1

if "%FOUND%"=="0" (
    echo [INFO] No matching java process found.
) else (
    echo [DONE] Completed.
)

exit /b 0

:handle_pid
set "PID=%~1"
set "IMAGE="
for /f "tokens=1 delims=," %%I in ('tasklist /FI "PID eq %PID%" /FO CSV /NH') do (
    set "IMAGE=%%~I"
)

if not defined IMAGE exit /b 0
if /I "%IMAGE%"=="INFO:" exit /b 0

if /I not "%IMAGE%"=="java.exe" if /I not "%IMAGE%"=="javaw.exe" (
    echo [SKIP] PID %PID% ^(%IMAGE%^) is not java/javaw.
    exit /b 0
)

set "FOUND=1"
if "%DRY_RUN%"=="1" (
    echo [DRYRUN] PID %PID% ^(%IMAGE%^) would be killed.
) else (
    echo [INFO] Killing PID %PID% ^(%IMAGE%^) ...
    taskkill /F /PID %PID% >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Failed to kill PID %PID% ^(already exited or access denied^).
    ) else (
        echo [OK] Killed PID %PID%
    )
)

exit /b 0

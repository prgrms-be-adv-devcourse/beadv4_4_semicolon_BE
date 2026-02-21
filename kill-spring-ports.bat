@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "PORTS=8081 8082 8083 8084 8085 8086 8087 8088"
set "PID_LIST="

echo [kill-spring-ports] Scanning Spring service ports: %PORTS%...

for %%P in (%PORTS%) do (
    for /F "tokens=5" %%I in ('netstat -ano -p tcp ^| findstr /R /C:":%%P[ ]" ^| findstr /I /C:"LISTENING"') do (
        if not "%%I"=="0" if not "%%I"=="4" (
            echo !PID_LIST! | findstr /R /C:"\<%%I\>" >nul
            if errorlevel 1 (
                set "PID_LIST=!PID_LIST! %%I"
                echo [kill-spring-ports] Port %%P -^> PID %%I
            )
        )
    )
)

if "%PID_LIST%"=="" (
    echo [kill-spring-ports] No listening process found in range.
    exit /b 0
)

echo [kill-spring-ports] Force killing PIDs:%PID_LIST%
for %%I in (%PID_LIST%) do (
    taskkill /PID %%I /F >nul 2^>^&1
    if errorlevel 1 (
        echo [kill-spring-ports] Failed to kill PID %%I
    ) else (
        echo [kill-spring-ports] Killed PID %%I
    )
)

exit /b 0

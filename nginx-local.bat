@echo off
setlocal enabledelayedexpansion

rem =========================================================
rem Local nginx helper for Windows.
rem Usage: nginx-local.bat [up^|down^|restart^|reload^|logs^|ps^|toggle] [pause^-p] [-y^|--yes]
rem =========================================================

set "SCRIPT_DIR=%~dp0"
set "COMPOSE_FILE=%SCRIPT_DIR%docker-compose.local.nginx.yml"
set "NGINX_CONF=%SCRIPT_DIR%nginx-conf\default.conf"
set "CERTS_DIR=%SCRIPT_DIR%nginx-conf\certs"
set "CERT_FULLCHAIN=%CERTS_DIR%\fullchain.pem"
set "CERT_PRIVKEY=%CERTS_DIR%\privkey.pem"
set "COMPOSE_CMD="

if "%BACKEND_DOCKER_NETWORK%"=="" (
    set "NETWORK_NAME=beadv4_4_semicolon_be_default"
) else (
    set "NETWORK_NAME=%BACKEND_DOCKER_NETWORK%"
)

set "ACTION=%~1"
if "%ACTION%"=="" set "ACTION=toggle"

set "PAUSE_AFTER=0"
set "FORCE_DOWN=0"
set "SKIP_PAUSE=0"
if "%~1"=="" set "PAUSE_AFTER=1"
for %%A in (%*) do (
    if /I "%%~A"=="pause" set "PAUSE_AFTER=1"
    if /I "%%~A"=="-p" set "PAUSE_AFTER=1"
    if /I "%%~A"=="-y" set "FORCE_DOWN=1"
    if /I "%%~A"=="--yes" set "FORCE_DOWN=1"
    if /I "%%~A"=="--no-pause" set "SKIP_PAUSE=1"
)
if "%SKIP_PAUSE%"=="1" set "PAUSE_AFTER=0"

docker compose version >nul 2>&1
if not errorlevel 1 (
    set "COMPOSE_CMD=docker compose"
) else (
    where docker-compose >nul 2>&1
    if not errorlevel 1 (
        set "COMPOSE_CMD=docker-compose"
    ) else (
        echo [nginx-local] Docker compose command not found. Install Docker Compose plugin or docker-compose.
        set "EXIT_CODE=1"
        goto :finish
    )
)

call :preflight
if errorlevel 1 goto :finish

if /I "%ACTION%"=="up" goto :up
if /I "%ACTION%"=="down" goto :down
if /I "%ACTION%"=="restart" goto :restart
if /I "%ACTION%"=="reload" goto :reload
if /I "%ACTION%"=="logs" goto :logs
if /I "%ACTION%"=="ps" goto :ps
if /I "%ACTION%"=="toggle" goto :toggle

echo [nginx-local] Unknown action: %ACTION%
echo Usage: nginx-local.bat [up^|down^|restart^|reload^|logs^|ps^|toggle] [pause^-p] [-y^|--yes] [--no-pause]
set "EXIT_CODE=1"
goto :finish

:preflight
where docker >nul 2>&1
if errorlevel 1 (
    echo [nginx-local] Docker command not found. Install Docker Desktop first.
    set "EXIT_CODE=1"
    exit /b 1
)

docker info >nul 2>&1
if errorlevel 1 (
    echo [nginx-local] Docker daemon is not running. Start Docker Desktop and retry.
    set "EXIT_CODE=1"
    exit /b 1
)

if not exist "%COMPOSE_FILE%" (
    echo [nginx-local] Compose file not found: "%COMPOSE_FILE%"
    set "EXIT_CODE=1"
    exit /b 1
)

if not exist "%NGINX_CONF%" (
    echo [nginx-local] Nginx config file not found: "%NGINX_CONF%"
    set "EXIT_CODE=1"
    exit /b 1
)
exit /b 0

:ensure_certs
if not exist "%CERTS_DIR%" (
    mkdir "%CERTS_DIR%" >nul 2>&1
)

if exist "%CERT_FULLCHAIN%" if exist "%CERT_PRIVKEY%" goto :eof

echo [nginx-local] TLS cert not found. Generating certs...

where mkcert >nul 2>&1
if not errorlevel 1 (
    echo [nginx-local] Using mkcert (browser-trusted)...
    mkcert -cert-file "%CERT_FULLCHAIN%" -key-file "%CERT_PRIVKEY%" api.dukku.shop localhost 127.0.0.1
) else (
    where openssl >nul 2>&1
    if not errorlevel 1 (
        echo [nginx-local] mkcert not found. Using local openssl fallback (self-signed, not browser-trusted)...
        openssl req -x509 -nodes -newkey rsa:2048 -keyout "%CERT_PRIVKEY%" -out "%CERT_FULLCHAIN%" -days 365 -subj /CN=localhost
    ) else (
        echo [nginx-local] mkcert not found. Using dockerized openssl fallback (self-signed, not browser-trusted)...
        docker run --rm -v "%CERTS_DIR%:/out" alpine:3.20 sh -lc "apk add --no-cache openssl > /dev/null ; openssl req -x509 -nodes -newkey rsa:2048 -keyout /out/privkey.pem -out /out/fullchain.pem -days 365 -subj /CN=localhost"
    )
)
if errorlevel 1 (
    echo [nginx-local] Failed to generate certs.
    set "EXIT_CODE=1"
    set "PAUSE_AFTER=1"
    goto :finish
)

if not exist "%CERT_FULLCHAIN%" (
    echo [nginx-local] Cert generation finished but fullchain.pem was not created.
    echo [nginx-local] Checked: "%CERT_FULLCHAIN%"
    dir /b "%CERTS_DIR%" | findstr /i "fullchain.pem privkey.pem"
    set "EXIT_CODE=1"
    set "PAUSE_AFTER=1"
    goto :finish
)

if not exist "%CERT_PRIVKEY%" (
    echo [nginx-local] Cert generation finished but privkey.pem was not created.
    echo [nginx-local] Checked: "%CERT_PRIVKEY%"
    dir /b "%CERTS_DIR%" | findstr /i "fullchain.pem privkey.pem"
    set "EXIT_CODE=1"
    set "PAUSE_AFTER=1"
    goto :finish
)
goto :eof

:up
docker network inspect "%NETWORK_NAME%" >nul 2>&1
if errorlevel 1 (
    echo [nginx-local] Docker network "%NETWORK_NAME%" not found. Creating...
    docker network create "%NETWORK_NAME%" >nul
    if errorlevel 1 (
        set "EXIT_CODE=1"
        goto :finish
    )
)

call :ensure_certs
if errorlevel 1 (
    echo [nginx-local] Failed to prepare cert files.
    set "EXIT_CODE=1"
    goto :finish
)

%COMPOSE_CMD% -f "%COMPOSE_FILE%" up -d nginx
set "EXIT_CODE=%errorlevel%"
goto :finish

:down
set "RUNNING_ID="
for /f "usebackq delims=" %%I in (`docker ps --filter "name=semicolon-nginx" --filter "status=running" -q`) do (
    set "RUNNING_ID=%%I"
    goto :down_exec_check
)
:down_exec_check
if defined RUNNING_ID goto :down_detected
goto :down_execute

:down_detected
if not "%FORCE_DOWN%"=="1" (
    echo [nginx-local] semicolon-nginx is currently UP.
    set /p CONFIRM_DOWN=Proceed with down? ^(y/N^): 
    if /I not "!CONFIRM_DOWN!"=="y" (
        echo [nginx-local] Canceled.
        set "EXIT_CODE=0"
        goto :finish
    )
)

:down_execute
%COMPOSE_CMD% -f "%COMPOSE_FILE%" down
set "EXIT_CODE=%errorlevel%"
goto :finish

:toggle
set "RUNNING_ID="
for /f "usebackq delims=" %%I in (`docker ps --filter "name=semicolon-nginx" --filter "status=running" -q`) do (
    set "RUNNING_ID=%%I"
    goto :toggle_exec_check
)
:toggle_exec_check
if defined RUNNING_ID (
    echo [nginx-local] semicolon-nginx is running. Stopping...
    %COMPOSE_CMD% -f "%COMPOSE_FILE%" down
    set "EXIT_CODE=%errorlevel%"
    goto :finish
)

echo [nginx-local] semicolon-nginx is not running. Starting...
goto :up

:restart
%COMPOSE_CMD% -f "%COMPOSE_FILE%" restart nginx
set "EXIT_CODE=%errorlevel%"
goto :finish

:reload
docker ps --filter "name=semicolon-nginx" --filter "status=running" -q | findstr /r /c:"." >nul
if errorlevel 1 (
    echo [nginx-local] semicolon-nginx is not running. Start first with: nginx-local.bat up
    set "EXIT_CODE=1"
    goto :finish
)

docker exec semicolon-nginx nginx -t
if errorlevel 1 (
    set "EXIT_CODE=1"
    goto :finish
)

docker exec semicolon-nginx nginx -s reload
set "EXIT_CODE=%errorlevel%"
goto :finish

:logs
%COMPOSE_CMD% -f "%COMPOSE_FILE%" logs -f nginx
set "EXIT_CODE=%errorlevel%"
goto :finish

:ps
%COMPOSE_CMD% -f "%COMPOSE_FILE%" ps
set "EXIT_CODE=%errorlevel%"
goto :finish

:finish
if not defined EXIT_CODE set "EXIT_CODE=0"
if not "%EXIT_CODE%"=="0" set "PAUSE_AFTER=1"
if "%PAUSE_AFTER%"=="1" (
    if "%SKIP_PAUSE%"=="1" goto :_nginx_finish_end
    echo.
    pause
)
:_nginx_finish_end
exit /b %EXIT_CODE%

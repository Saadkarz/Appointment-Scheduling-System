@echo off
echo ========================================
echo   Appointment System - Quick Start
echo ========================================
echo.

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not running!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo [1/4] Checking environment files...

if not exist "backend\.env" (
    echo Creating backend\.env from template...
    copy "backend\.env.example" "backend\.env"
    echo.
    echo WARNING: Please edit backend\.env and configure your settings!
    echo Required: DB_PASSWORD, JWT_SECRET
    echo Optional: GOOGLE_CLIENT_ID, TWILIO_*, SENDGRID_API_KEY
    echo.
)

if not exist "frontend\.env" (
    echo Creating frontend\.env from template...
    copy "frontend\.env.example" "frontend\.env"
)

echo [2/4] Stopping any existing containers...
docker-compose down

echo [3/4] Building and starting services...
echo This may take several minutes on first run...
docker-compose up -d --build

echo [4/4] Waiting for services to be ready...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   Services Started Successfully!
echo ========================================
echo.
echo Frontend:    http://localhost:5173
echo Backend API: http://localhost:8081/api
echo phpMyAdmin:  http://localhost:8080
echo   - Username: root
echo   - Password: rootpassword
echo.
echo To view logs: docker-compose logs -f
echo To stop:      docker-compose down
echo.
echo Next: Open http://localhost:5173 and sign up!
echo.
pause

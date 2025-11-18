@echo off
echo ========================================
echo   Stopping Appointment System
echo ========================================
echo.

docker-compose down

echo.
echo All services stopped.
echo.
echo To remove all data (including database):
echo   docker-compose down -v
echo.
pause

@echo off
echo Building University ERP...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting University ERP...
echo.
call mvn exec:java
pause


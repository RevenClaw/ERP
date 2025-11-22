# PowerShell script to run the application
Write-Host "Building University ERP..." -ForegroundColor Cyan
mvn compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "Starting University ERP..." -ForegroundColor Green
Write-Host ""
mvn exec:java

Read-Host "Press Enter to exit"


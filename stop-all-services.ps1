# Cinema Management System - Stop All Services
# PowerShell Script (Microservices Only)

Write-Host ""
Write-Host "============================================" -ForegroundColor Red
Write-Host "Stopping Cinema Management Microservices" -ForegroundColor Red
Write-Host "============================================" -ForegroundColor Red
Write-Host ""

Write-Host "Stopping all Java processes (Spring Boot services)..." -ForegroundColor Yellow

# Stop all PowerShell windows running mvnw
Get-Process | Where-Object {$_.MainWindowTitle -like "*mvnw*" -or $_.MainWindowTitle -like "*Eureka*" -or $_.MainWindowTitle -like "*Service*" -or $_.MainWindowTitle -like "*Gateway*"} | Stop-Process -Force -ErrorAction SilentlyContinue

# Kill processes on specific ports
$ports = @(8761, 8081, 8082, 8083, 8084, 8085, 8888)
foreach ($port in $ports) {
    $process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($process) {
        Write-Host "  Stopping process on port $port (PID: $process)" -ForegroundColor Yellow
        Stop-Process -Id $process -Force -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "All services stopped!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Note: Docker containers (MySQL + Loki + Grafana) are still running." -ForegroundColor Yellow
Write-Host "To stop Docker: docker compose down" -ForegroundColor Cyan
Write-Host ""
Read-Host "Press Enter to close this window"

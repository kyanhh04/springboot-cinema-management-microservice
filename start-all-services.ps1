# Cinema Management System - Start All Services
# PowerShell Script (Microservices Only)

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Starting Cinema Management Microservices" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

# Function to check if port is in use
function Test-Port {
    param($Port)
    $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue
    return $connection.TcpTestSucceeded
}

Write-Host "Checking required ports..." -ForegroundColor Cyan
$ports = @(8761, 8081, 8082, 8083, 8084, 8085, 8888)
foreach ($port in $ports) {
    if (Test-Port $port) {
        Write-Host "Warning: Port $port is already in use" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Note: Make sure Docker containers are running!" -ForegroundColor Yellow
Write-Host "  Run: docker compose up -d" -ForegroundColor Cyan
Write-Host ""

Write-Host ""
Write-Host "[1/7] Starting Eureka Server..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\eureka-server\eureka-server'; .\mvnw spring-boot:run" -WindowStyle Normal
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "[2/7] Starting User Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\user-service'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "[3/7] Starting Cinema Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\cinema-service\cinema-service'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "[4/7] Starting Booking Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\booking-service\booking-service'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "[5/7] Starting Inventory Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\inventory-service'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "[6/7] Starting Notification Service..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\notification-service'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "Waiting for services to register with Eureka (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "[7/7] Starting Gateway..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD\gateway'; .\mvnw spring-boot:run" -WindowStyle Normal

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "All services are starting!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Microservices:" -ForegroundColor Cyan
Write-Host "- Eureka Server:        http://localhost:8761" -ForegroundColor White
Write-Host "- User Service:         http://localhost:8081" -ForegroundColor White
Write-Host "- Notification Service: http://localhost:8082" -ForegroundColor White
Write-Host "- Inventory Service:    http://localhost:8083" -ForegroundColor White
Write-Host "- Cinema Service:       http://localhost:8084" -ForegroundColor White
Write-Host "- Booking Service:      http://localhost:8085" -ForegroundColor White
Write-Host "- Gateway:              http://localhost:8888" -ForegroundColor White
Write-Host ""
Write-Host "Opening Eureka Dashboard in 5 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
Start-Process "http://localhost:8761"

Write-Host ""
Write-Host "All services started successfully!" -ForegroundColor Green
Write-Host "Check the individual PowerShell windows for logs." -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to close this window"

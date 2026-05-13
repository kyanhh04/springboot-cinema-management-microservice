# Cinema Management System - Start All Services
# PowerShell Script (Microservices Only)

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Starting Cinema Management Microservices" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

$repoRoot = $PSScriptRoot

# Function to check if port is in use
function Test-Port {
    param($Port)
    $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue
    return $connection.TcpTestSucceeded
}

function Wait-Port {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [int]$TimeoutSeconds = 120
    )

    Write-Host "Waiting for $Name on port $Port..." -ForegroundColor Yellow
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        if (Test-Port $Port) {
            Write-Host "  $Name is ready" -ForegroundColor Green
            return
        }

        Start-Sleep -Seconds 2
    }

    throw "$Name did not become ready on port $Port within $TimeoutSeconds seconds."
}

# Resolve the actual Maven project directory for a service.
function Resolve-ServicePath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ServiceName
    )

    $serviceRoot = Join-Path $repoRoot $ServiceName
    if (-not (Test-Path $serviceRoot)) {
        throw "Service directory not found: $serviceRoot"
    }

    $rootPom = Join-Path $serviceRoot "pom.xml"
    if (Test-Path $rootPom) {
        return $serviceRoot
    }

    $nestedPom = Get-ChildItem -Path $serviceRoot -Directory |
        ForEach-Object { Join-Path $_.FullName "pom.xml" } |
        Where-Object { Test-Path $_ } |
        Select-Object -First 1

    if ($nestedPom) {
        return Split-Path $nestedPom -Parent
    }

    throw "No pom.xml found for service: $ServiceName"
}

function Start-ServiceWindow {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ServiceName
    )

    $servicePath = Resolve-ServicePath -ServiceName $ServiceName
    Write-Host "  Path: $servicePath" -ForegroundColor DarkGray
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "Set-Location '$servicePath'; mvn spring-boot:run" -WindowStyle Normal
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Maven (mvn) was not found in PATH. Install Maven or add it to PATH, then run this script again." -ForegroundColor Red
    Read-Host "Press Enter to close this window"
    exit 1
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker was not found in PATH. Start MySQL and RabbitMQ manually, then run this script again." -ForegroundColor Red
    Read-Host "Press Enter to close this window"
    exit 1
}

Write-Host "Checking required ports..." -ForegroundColor Cyan
$ports = @(8761, 8081, 8082, 8083, 8084, 8085, 8888)
foreach ($port in $ports) {
    if (Test-Port $port) {
        Write-Host "Warning: Port $port is already in use" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Starting infrastructure containers..." -ForegroundColor Cyan
Push-Location $repoRoot
docker compose up -d
if ($LASTEXITCODE -ne 0) {
    Pop-Location
    Write-Host "Failed to start Docker containers. Check Docker Desktop and docker-compose.yml." -ForegroundColor Red
    Read-Host "Press Enter to close this window"
    exit 1
}
Pop-Location

Wait-Port -Port 5672 -Name "RabbitMQ"
Wait-Port -Port 3310 -Name "Booking MySQL"
Wait-Port -Port 3308 -Name "Cinema MySQL"
Wait-Port -Port 3309 -Name "Inventory MySQL"
Wait-Port -Port 3311 -Name "User MySQL"
Wait-Port -Port 3312 -Name "Notification MySQL"
Write-Host ""

Write-Host ""
Write-Host "[1/7] Starting Eureka Server..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "eureka-server"
Start-Sleep -Seconds 15

Write-Host ""
Write-Host "[2/7] Starting User Service..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "user-service"

Write-Host ""
Write-Host "[3/7] Starting Cinema Service..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "cinema-service"

Write-Host ""
Write-Host "[4/7] Starting Booking Service..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "booking-service"

Write-Host ""
Write-Host "[5/7] Starting Inventory Service..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "inventory-service"

Write-Host ""
Write-Host "[6/7] Starting Notification Service..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "notification-service"

Write-Host ""
Write-Host "Waiting for services to register with Eureka (30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host ""
Write-Host "[7/7] Starting Gateway..." -ForegroundColor Green
Start-ServiceWindow -ServiceName "gateway"

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "All services are starting!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "Infrastructure:" -ForegroundColor Cyan
Write-Host "- Loki:                 http://localhost:3100" -ForegroundColor White
Write-Host "- Grafana:              http://localhost:3000 (admin/admin)" -ForegroundColor White
Write-Host "- RabbitMQ:             http://localhost:15672 (guest/guest)" -ForegroundColor White
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
Write-Host "Logging:" -ForegroundColor Cyan
Write-Host "- View logs in Grafana: http://localhost:3000" -ForegroundColor White
Write-Host "- Query example: {app=`"user-service`"}" -ForegroundColor Gray
Write-Host ""
Write-Host "Opening Eureka Dashboard in 5 seconds..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
Start-Process "http://localhost:8761"

Write-Host ""
Write-Host "All services started successfully!" -ForegroundColor Green
Write-Host "Check the individual PowerShell windows for logs." -ForegroundColor Yellow
Write-Host ""
Read-Host "Press Enter to close this window"

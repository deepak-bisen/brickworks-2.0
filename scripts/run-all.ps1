<#
.SYNOPSIS
  Start BrickWorkPro 2.0 in ONE PowerShell window: DB, backend, frontend, then live logs.

.USAGE
  powershell -ExecutionPolicy Bypass -File scripts\run-all.ps1
  powershell -ExecutionPolicy Bypass -File scripts\run-all.ps1 -Fresh

  Ctrl+C stops the log viewer only. Services keep running.
  To stop everything: scripts\stop-all.ps1
#>
param(
    [switch]$SkipBuild,
    [switch]$SkipDb,
    [switch]$Fresh,
    [switch]$NoFrontend,
    [switch]$NoLogs
)

$ErrorActionPreference = "Stop"
$Root = Split-Path $PSScriptRoot -Parent
$Backend = Join-Path $Root "BrickWorkPro-2.0-Backend"
$Frontend = Join-Path $Root "BrickWorkPro-2.0-Frontend"
$GatewayJar = Join-Path $Backend "api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"
$LogDir = Join-Path $PSScriptRoot "logs"

function Write-Step($msg) { Write-Host "`n==> $msg" -ForegroundColor Cyan }
function Test-Command($name) {
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if (-not $cmd) { throw "Required command not found: $name" }
}

function Wait-ForPort($port, $label, $timeoutSec = 120) {
    $deadline = (Get-Date).AddSeconds($timeoutSec)
    while ((Get-Date) -lt $deadline) {
        if (Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue) {
            Write-Host "  $label is listening on port $port" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
    }
    Write-Host "  WARNING: $label not detected on port $port yet" -ForegroundColor Yellow
    return $false
}

function Initialize-Databases {
    $initSql = Join-Path $PSScriptRoot "init-databases.sql"
    if (-not (Test-Path $initSql)) { return }
    $mysql = Get-Command mysql -ErrorAction SilentlyContinue
    if (-not $mysql) { return }

    Write-Step "Ensuring databases exist"
    try {
        Get-Content $initSql | & mysql -u root -proot 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "  Could not run init-databases.sql (check root password)." -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  Could not run init-databases.sql." -ForegroundColor Yellow
    }
}

function Start-BackgroundFrontend($frontendDir, $logFile) {
    New-Item -ItemType Directory -Force -Path (Split-Path $logFile) | Out-Null
    $errFile = Join-Path (Split-Path $logFile) "frontend-err.log"
    if (Test-Path $logFile) { Remove-Item $logFile -Force -ErrorAction SilentlyContinue }
    if (Test-Path $errFile) { Remove-Item $errFile -Force -ErrorAction SilentlyContinue }
    $npm = (Get-Command npm.cmd).Source
    Start-Process -FilePath $npm `
        -ArgumentList "start" `
        -WorkingDirectory $frontendDir `
        -WindowStyle Hidden `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errFile
}

Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "  BrickWorkPro 2.0 - One-Window Launcher" -ForegroundColor Magenta
Write-Host "========================================`n" -ForegroundColor Magenta

Write-Step "Checking prerequisites"
Test-Command "java"
if (-not $NoFrontend) { Test-Command "npm" }

if ($Fresh) {
    Write-Step "Stopping existing Java / Node processes"
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Get-Process node -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

if (-not $SkipDb) {
    Write-Step "Starting MariaDB"
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($docker) {
        Push-Location $Root
        try {
            docker compose up -d 2>&1 | ForEach-Object { Write-Host $_ }
        } finally { Pop-Location }
    } else {
        Write-Host "  Docker not found - using local MariaDB on 3306" -ForegroundColor Yellow
    }
}

Wait-ForPort 3306 "MariaDB" 60 | Out-Null
if (-not (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue)) {
    throw "MariaDB not on port 3306. Start MariaDB or run: docker compose up -d"
}

Initialize-Databases

if (-not (Test-Path $GatewayJar)) {
    if ($SkipBuild) { throw "Gateway JAR missing. Run without -SkipBuild." }
    Write-Step "Building backend (first run - several minutes)"
    Test-Command "mvn"
    Push-Location $Backend
    try {
        mvn install -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }
    } finally { Pop-Location }
} else {
    Write-Host "  Backend JARs found - skipping build" -ForegroundColor DarkGray
}

if (-not $NoFrontend) {
    if (-not (Test-Path (Join-Path $Frontend "node_modules"))) {
        Write-Step "Installing frontend (npm install)"
        Push-Location $Frontend
        try {
            npm.cmd install
            if ($LASTEXITCODE -ne 0) { throw "npm install failed" }
        } finally { Pop-Location }
    }
}

Write-Step "Starting backend (background, same window)"
& (Join-Path $PSScriptRoot "start-backend.ps1")

if (-not $NoFrontend) {
    Write-Step "Starting frontend (background, same window)"
    $frontendLog = Join-Path $LogDir "frontend.log"
    Start-BackgroundFrontend $Frontend $frontendLog
    Write-Host "  Frontend starting -> http://localhost:4200" -ForegroundColor Green
}

Write-Step "Waiting for ports"
$ports = @(8761, 8083, 8081, 8080, 8084, 9191)
if (-not $NoFrontend) { $ports += 4200 }
Start-Sleep -Seconds 10
foreach ($p in $ports) {
    $label = switch ($p) {
        8761 { "Eureka" }; 8083 { "Users" }; 8081 { "Products" }
        8080 { "Orders" }; 8084 { "Finance" }; 9191 { "Gateway" }
        4200 { "Frontend" }; default { "Service" }
    }
    Wait-ForPort $p $label 90 | Out-Null
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  BrickWorkPro 2.0 is UP" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "  UI:      http://localhost:4200"
Write-Host "  API:     http://localhost:9191"
Write-Host "  Admin:   superadmin / Admin@123"
Write-Host "  Stop:    scripts\stop-all.ps1"
Write-Host "  Ctrl+C   stops log view only (services keep running)"
Write-Host ""

if (-not $NoLogs) {
    & (Join-Path $PSScriptRoot "tail-logs.ps1") -Tail 5
}
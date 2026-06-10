<#
.SYNOPSIS
  Stop BrickWorkPro 2.0 local dev processes without killing unrelated Java apps (e.g. IntelliJ).

.USAGE
  powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
  powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1 -KeepDb
#>
param(
    [switch]$KeepDb
)

$ErrorActionPreference = "SilentlyContinue"
$Root = Split-Path $PSScriptRoot -Parent
$Backend = Join-Path $Root "BrickWorkPro-2.0-Backend"

# BrickWorkPro service ports (Eureka, Users, Products, Orders, Finance, Gateway, Frontend)
$ports = @(8761, 8083, 8081, 8080, 8084, 9191, 4200)
$stopped = New-Object 'System.Collections.Generic.HashSet[int]'

function Stop-ProcessById($processId, $reason) {
    if (-not $processId -or $processId -le 0) { return }
    if ($stopped.Contains($processId)) { return }

    $proc = Get-Process -Id $processId -ErrorAction SilentlyContinue
    if (-not $proc) { return }

    Write-Host "  Stopping PID $processId ($($proc.ProcessName)) - $reason"
    Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    [void]$stopped.Add($processId)
}

function Stop-ProcessOnPort($port) {
    $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($conn in $connections) {
        Stop-ProcessById $conn.OwningProcess "port $port"
    }
}

Write-Host "Stopping BrickWorkPro services by port..."
foreach ($p in $ports) {
    Stop-ProcessOnPort $p
}

Write-Host "Stopping BrickWorkPro java -jar processes..."
Get-CimInstance Win32_Process -Filter "Name='java.exe'" -ErrorAction SilentlyContinue |
    Where-Object {
        $_.CommandLine -and (
            $_.CommandLine -like "*BrickWorkPro-2.0-Backend*" -or
            $_.CommandLine -like "*brickwork*" -or
            $_.CommandLine -like "*SNAPSHOT.jar*"
        )
    } |
    ForEach-Object {
        Stop-ProcessById $_.ProcessId "java -jar backend"
    }

Write-Host "Stopping BrickWorkPro frontend (node)..."
Get-CimInstance Win32_Process -Filter "Name='node.exe'" -ErrorAction SilentlyContinue |
    Where-Object { $_.CommandLine -and $_.CommandLine -like "*BrickWorkPro-2.0-Frontend*" } |
    ForEach-Object {
        Stop-ProcessById $_.ProcessId "frontend dev server"
    }

if (-not $KeepDb) {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($docker) {
        Push-Location $Root
        docker compose down 2>&1 | ForEach-Object { Write-Host $_ }
        Pop-Location
        Write-Host "Docker MariaDB stopped."
    }
}

Write-Host "Done. BrickWorkPro dev processes stopped (IntelliJ / other Java apps left running)."
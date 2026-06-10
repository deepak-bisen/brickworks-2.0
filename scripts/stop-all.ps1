<#
.SYNOPSIS
  Stop BrickWorkPro 2.0 local dev processes (Java microservices, optional Docker DB).
#>
param(
    [switch]$KeepDb
)

$ErrorActionPreference = "SilentlyContinue"
$Root = Split-Path $PSScriptRoot -Parent

Write-Host "Stopping Java backend processes..."
Get-Process java | Stop-Process -Force

Write-Host "Stopping Node dev server..."
Get-Process node | Stop-Process -Force

if (-not $KeepDb) {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($docker) {
        Push-Location $Root
        docker compose down 2>&1 | ForEach-Object { Write-Host $_ }
        Pop-Location
        Write-Host "Docker MariaDB stopped."
    }
}

Write-Host "Done. All BrickWorkPro dev processes stopped."
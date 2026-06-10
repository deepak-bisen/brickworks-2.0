<#
.SYNOPSIS
  Stream all BrickWorkPro service logs in one terminal with labels.

.USAGE
  powershell -ExecutionPolicy Bypass -File scripts\tail-logs.ps1
  powershell -ExecutionPolicy Bypass -File scripts\tail-logs.ps1 -Tail 50
  powershell -ExecutionPolicy Bypass -File scripts\tail-logs.ps1 -IncludeErrors

  Usually called automatically by run-all.ps1 (one window).
  Or run alone: scripts\tail-logs.ps1
  Ctrl+C stops log view only; services keep running. Use stop-all.ps1 to stop app.
#>
param(
    [int]$Tail = 15,
    [switch]$IncludeErrors,
    [int]$PollMs = 400
)

$ErrorActionPreference = "SilentlyContinue"
$logDir = Join-Path $PSScriptRoot "logs"

if (-not (Test-Path $logDir)) {
    Write-Host "No logs folder yet. Start the app first:" -ForegroundColor Yellow
    Write-Host "  powershell -ExecutionPolicy Bypass -File scripts\run-all.ps1"
    exit 1
}

function Get-LogColor($name) {
    switch -Regex ($name) {
        '^eureka'     { 'Magenta' }
        '^gateway'    { 'Cyan' }
        '^users'      { 'Blue' }
        '^products'   { 'Green' }
        '^orders'     { 'Yellow' }
        '^finance'    { 'DarkYellow' }
        '^frontend'   { 'White' }
        '-err$'       { 'Red' }
        default       { 'Gray' }
    }
}

function Write-LogLine($tag, $line) {
    $color = Get-LogColor $tag
    $stamp = Get-Date -Format 'HH:mm:ss'
    Write-Host "[$stamp][$tag] " -NoNewline -ForegroundColor DarkGray
    Write-Host $line -ForegroundColor $color
}

function Get-LogFiles {
    $pattern = if ($IncludeErrors) { "*.log" } else { "*.log" }
    Get-ChildItem $logDir -Filter $pattern -File |
        Where-Object { $IncludeErrors -or $_.Name -notlike '*-err.log' } |
        Sort-Object Name
}

$files = Get-LogFiles
if ($files.Count -eq 0) {
    Write-Host "No log files in $logDir" -ForegroundColor Yellow
    Write-Host "Start the backend first: scripts\start-backend.ps1 or scripts\run-all.ps1"
    exit 1
}

# Track byte/line position per file
$state = @{}
foreach ($f in $files) {
    $state[$f.FullName] = @{
        Tag      = $f.BaseName
        Position = 0
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  BrickWorkPro - Unified Log Viewer" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Folder: $logDir"
Write-Host "  Files:  $($files.Name -join ', ')"
Write-Host "  Tail:   $Tail lines per file on start"
Write-Host "  Press Ctrl+C to stop"
Write-Host ""

# Show recent history from each file
foreach ($f in $files) {
    if (-not (Test-Path $f.FullName)) { continue }
    $lines = Get-Content $f.FullName -ErrorAction SilentlyContinue
    if (-not $lines) {
        $state[$f.FullName].Position = 0
        continue
    }
    $start = [Math]::Max(0, $lines.Count - $Tail)
    for ($i = $start; $i -lt $lines.Count; $i++) {
        Write-LogLine $state[$f.FullName].Tag $lines[$i]
    }
    $state[$f.FullName].Position = $lines.Count
}

Write-Host ""
Write-Host "--- live tail (new lines below) ---" -ForegroundColor DarkCyan
Write-Host ""

# Live tail loop
try {
    while ($true) {
        # Pick up new log files if services just started
        foreach ($f in Get-LogFiles) {
            if (-not $state.ContainsKey($f.FullName)) {
                $state[$f.FullName] = @{ Tag = $f.BaseName; Position = 0 }
            }
        }

        foreach ($entry in $state.GetEnumerator()) {
            $path = $entry.Key
            $tag = $entry.Value.Tag
            $pos = $entry.Value.Position

            if (-not (Test-Path $path)) { continue }

            $lines = Get-Content $path -ErrorAction SilentlyContinue
            if (-not $lines) { continue }

            if ($lines.Count -gt $pos) {
                for ($i = $pos; $i -lt $lines.Count; $i++) {
                    Write-LogLine $tag $lines[$i]
                }
                $state[$path].Position = $lines.Count
            }
        }

        Start-Sleep -Milliseconds $PollMs
    }
} finally {
    Write-Host "`nLog viewer stopped. Services are still running." -ForegroundColor Yellow
    Write-Host "Stop app: powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1" -ForegroundColor Yellow
}
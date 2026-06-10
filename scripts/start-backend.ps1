$ErrorActionPreference = "Stop"

$Root = Split-Path $PSScriptRoot -Parent
$backend = Join-Path $Root "BrickWorkPro-2.0-Backend"
$logs = Join-Path $PSScriptRoot "logs"
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$env:JWT_SECRET = "brickwork-dev-jwt-secret-key-32chars-min"
$env:INTERNAL_SERVICE_KEY = "brickworks-internal-dev-key"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
$env:RAZORPAY_KEY_ID = "rzp_test_SlCAfQpHujynkq"
$env:RAZORPAY_KEY_SECRET = "pASdjNg7LttbV2vikmK92oFU"
$env:RAZORPAY_WEBHOOK_SECRET = "dummy_webhook_secret"

$services = @(
    @{ Name = "eureka"; Jar = Join-Path $backend "service-registry-brickwork\target\service-registry-brickwork-0.0.1-SNAPSHOT.jar"; Port = 8761 },
    @{ Name = "users"; Jar = Join-Path $backend "users-brickwork\users-app\target\users-app-0.0.1-SNAPSHOT.jar"; Port = 8083 },
    @{ Name = "products"; Jar = Join-Path $backend "products-brickwork\products-app\target\products-app-0.0.1-SNAPSHOT.jar"; Port = 8081 },
    @{ Name = "orders"; Jar = Join-Path $backend "orders-brickwork\orders-app\target\orders-app-0.0.1-SNAPSHOT.jar"; Port = 8080 },
    @{ Name = "finance"; Jar = Join-Path $backend "finance-brickwork\finance-app\target\finance-app-0.0.1-SNAPSHOT.jar"; Port = 8084 },
    @{ Name = "gateway"; Jar = Join-Path $backend "api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"; Port = 9191 }
)

function Start-BackgroundJava($jarPath, $logFile, $errFile) {
    $java = (Get-Command java).Source
    $wd = Split-Path $jarPath
    Start-Process -FilePath $java `
        -ArgumentList "-jar", $jarPath `
        -WorkingDirectory $wd `
        -WindowStyle Hidden `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errFile
}

foreach ($svc in $services) {
    if (-not (Test-Path $svc.Jar)) {
        throw "Missing JAR: $($svc.Jar). Run: cd BrickWorkPro-2.0-Backend; mvn install -DskipTests"
    }

    $logFile = Join-Path $logs "$($svc.Name).log"
    $errFile = Join-Path $logs "$($svc.Name)-err.log"
    if (Test-Path $logFile) { Remove-Item $logFile -Force -ErrorAction SilentlyContinue }
    if (Test-Path $errFile) { Remove-Item $errFile -Force -ErrorAction SilentlyContinue }

    Start-BackgroundJava $svc.Jar $logFile $errFile
    Write-Host "Started $($svc.Name) -> port $($svc.Port)" -ForegroundColor Green

    if ($svc.Name -eq "eureka") { Start-Sleep -Seconds 25 }
    elseif ($svc.Name -in @("users","products","orders","finance")) { Start-Sleep -Seconds 15 }
    else { Start-Sleep -Seconds 30 }
}

Write-Host "Waiting for gateway to discover all services..."
Start-Sleep -Seconds 30
Write-Host "All backend services started (background, no extra windows)." -ForegroundColor Green
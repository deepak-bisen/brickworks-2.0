$ErrorActionPreference = "Stop"
$env:JWT_SECRET = "brickwork-dev-jwt-secret-key-32chars-min"
$env:INTERNAL_SERVICE_KEY = "brickworks-internal-dev-key"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "root"
$env:RAZORPAY_KEY_ID = "rzp_test_SlCAfQpHujynkq"
$env:RAZORPAY_KEY_SECRET = "pASdjNg7LttbV2vikmK92oFU"
$env:RAZORPAY_WEBHOOK_SECRET = "dummy_webhook_secret"

$backend = "D:\Projects\Other\BrickWorkPro-2.0\BrickWorkPro-2.0-Backend"
$logs = "D:\Projects\Other\BrickWorkPro-2.0\scripts\logs"
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$services = @(
    @{ Name = "eureka"; Jar = "$backend\service-registry-brickwork\target\service-registry-brickwork-0.0.1-SNAPSHOT.jar"; Port = 8761 },
    @{ Name = "users"; Jar = "$backend\users-brickwork\users-app\target\users-app-0.0.1-SNAPSHOT.jar"; Port = 8083 },
    @{ Name = "products"; Jar = "$backend\products-brickwork\products-app\target\products-app-0.0.1-SNAPSHOT.jar"; Port = 8081 },
    @{ Name = "orders"; Jar = "$backend\orders-brickwork\orders-app\target\orders-app-0.0.1-SNAPSHOT.jar"; Port = 8080 },
    @{ Name = "finance"; Jar = "$backend\finance-brickwork\finance-app\target\finance-app-0.0.1-SNAPSHOT.jar"; Port = 8084 },
    @{ Name = "gateway"; Jar = "$backend\api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar"; Port = 9191 }
)

foreach ($svc in $services) {
    $logFile = Join-Path $logs "$($svc.Name).log"
    $errFile = Join-Path $logs "$($svc.Name)-err.log"
    # Append logs without holding pipes open (avoids child JVM exit on Windows)
    $args = @(
        "-NoProfile", "-Command",
        "`$env:JWT_SECRET='$env:JWT_SECRET'; `$env:INTERNAL_SERVICE_KEY='$env:INTERNAL_SERVICE_KEY'; " +
        "`$env:DB_USERNAME='$env:DB_USERNAME'; `$env:DB_PASSWORD='$env:DB_PASSWORD'; " +
        "`$env:RAZORPAY_KEY_ID='$env:RAZORPAY_KEY_ID'; `$env:RAZORPAY_KEY_SECRET='$env:RAZORPAY_KEY_SECRET'; " +
        "`$env:RAZORPAY_WEBHOOK_SECRET='$env:RAZORPAY_WEBHOOK_SECRET'; " +
        "java -jar '$($svc.Jar)' *>> '$logFile' 2>> '$errFile'"
    )
    Start-Process -FilePath "powershell.exe" -ArgumentList $args -WindowStyle Minimized
    Write-Host "Started $($svc.Name) -> port $($svc.Port)"
    if ($svc.Name -eq "eureka") { Start-Sleep -Seconds 25 }
    elseif ($svc.Name -in @("users","products","orders","finance")) { Start-Sleep -Seconds 15 }
    else { Start-Sleep -Seconds 30 }
}

Write-Host "Waiting for gateway to discover all services..."
Start-Sleep -Seconds 30
Write-Host "All backend services started. Logs: $logs"
Write-Host "Tip: wait ~60s after this script before running test-guest-workflow.ps1"
$ErrorActionPreference = "Stop"
$base = "http://localhost:9191"
$passed = 0; $failed = 0

function Test-Step($name, $scriptBlock) {
    try { & $scriptBlock; Write-Host "[PASS] $name" -ForegroundColor Green; $script:passed++ }
    catch { Write-Host "[FAIL] $name - $($_.Exception.Message)" -ForegroundColor Red; $script:failed++ }
}

Write-Host "`n=== Guest E-Commerce Workflow ===`n"

Test-Step "Products catalog" {
    $r = Invoke-RestMethod -Uri "$base/api/products/all" -Method GET -TimeoutSec 120
    if (-not $r -or (@($r).Count -eq 0)) { throw "No products" }
    $script:productId = @($r)[0].productId
}

Test-Step "Create guest order" {
    $body = @{
        customerName = "Guest Buyer"; customerEmail = "g@t.com"; customerPhone = "9876543210"
        deliveryAddress = "Test Site Jaipur"
        items = @(@{ productId = $script:productId; quantity = 500 })
    } | ConvertTo-Json -Depth 5
    $r = Invoke-RestMethod -Uri "$base/api/orders/create" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60
    $script:orderId = $r.orderId; $script:amount = $r.totalAmount
    if (-not $script:orderId) { throw "No orderId" }
}

Test-Step "COD payment" {
    $body = @{ orderId = $script:orderId; amount = $script:amount } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/finance/payments/cod/select" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60 | Out-Null
}

Test-Step "Track order" {
    $r = Invoke-RestMethod -Uri "$base/api/orders/track?orderId=$($script:orderId)&phone=9876543210" -Method GET -TimeoutSec 30
    if ($r.orderId -ne $script:orderId) { throw "Track mismatch" }
}

Test-Step "Public quote" {
    $body = @{
        customerName = "Lead"; customerEmail = "l@t.com"; customerPhone = "9123456789"
        deliveryAddress = "Site A"
        items = @(@{ productId = $script:productId; quantity = 500 })
    } | ConvertTo-Json -Depth 5
    Invoke-RestMethod -Uri "$base/api/orders/public-quote" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 60 | Out-Null
}

Test-Step "UTR verify requires admin" {
    try {
        Invoke-RestMethod -Uri "$base/api/finance/payments/utr/verify/$($script:orderId)?approved=true" -Method POST -TimeoutSec 15 -ErrorAction Stop | Out-Null
        throw "Should require auth"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -notin 401,403) { throw $_ }
    }
}

Write-Host "`n=== Results: $passed passed, $failed failed ===`n"
if ($failed -gt 0) { exit 1 }
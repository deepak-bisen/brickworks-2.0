$ErrorActionPreference = "Stop"
$base = "http://localhost:9191"
$passed = 0
$failed = 0

function Test-Step($name, $scriptBlock) {
    try {
        & $scriptBlock
        Write-Host "[PASS] $name" -ForegroundColor Green
        $script:passed++
    } catch {
        Write-Host "[FAIL] $name - $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
    }
}

Write-Host "`n=== BrickWorkPro Workflow Test ===`n"

# 1. Gateway health - products list
Test-Step "GET /api/products/all" {
    $r = Invoke-RestMethod -Uri "$base/api/products/all" -Method GET
    if ($null -eq $r) { throw "Empty response" }
}

# 2. Register test customer
$customerUser = "tc$(Get-Random -Maximum 99999)"
$customerPass = "Test@12345"
Test-Step "POST /api/auth/register/customer" {
    $body = @{
        username = $customerUser
        password = $customerPass
        fullName = "Test Customer"
        email = "$customerUser@t.com"
        phoneNumber = "9876543210"
        billingAddress = "Test Address, Jaipur"
        customerType = "INDIVIDUAL"
    } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/auth/register/customer" -Method POST -Body $body -ContentType "application/json" | Out-Null
}

# 3. Customer login
$customerToken = $null
Test-Step "POST /api/auth/login (customer)" {
    $body = @{ username = $customerUser; password = $customerPass } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$base/api/auth/login" -Method POST -Body $body -ContentType "application/json"
    if (-not $r.token) { throw "No token returned" }
    $script:customerToken = $r.token
}

# 4. Get products and create order
$productId = $null
$orderId = $null
$orderAmount = $null
Test-Step "POST /api/orders/create (checkout)" {
    $products = Invoke-RestMethod -Uri "$base/api/products/all" -Method GET
    $list = if ($products -is [array]) { $products } elseif ($products.data) { $products.data } else { @($products) }
    if ($list.Count -eq 0) { throw "No products available" }
    $script:productId = $list[0].productId
    $body = @{
        customerName = "Test Customer"
        customerEmail = "$customerUser@test.com"
        customerPhone = "9876543210"
        deliveryAddress = "Test Address, Jaipur"
        items = @(@{ productId = $script:productId; quantity = 500 })
    } | ConvertTo-Json -Depth 5
    $headers = @{ Authorization = "Bearer $script:customerToken" }
    $r = Invoke-RestMethod -Uri "$base/api/orders/create" -Method POST -Body $body -ContentType "application/json" -Headers $headers
    if (-not $r.orderId) { throw "No orderId" }
    $script:orderId = $r.orderId
    $script:orderAmount = if ($r.totalAmount) { $r.totalAmount } else { 1000 }
}

# 5. COD payment
Test-Step "POST /api/finance/payments/cod/select" {
    $body = @{ orderId = $script:orderId; amount = $script:orderAmount } | ConvertTo-Json
    Invoke-RestMethod -Uri "$base/api/finance/payments/cod/select" -Method POST -Body $body -ContentType "application/json" | Out-Null
}

# 6. Track order (guest)
Test-Step "GET /api/orders/track" {
    $r = Invoke-RestMethod -Uri "$base/api/orders/track?orderId=$($script:orderId)&phone=9876543210" -Method GET
    if (-not $r.orderId) { throw "Track failed" }
}

# 7. Customer views own order
Test-Step "GET /api/orders/{id} (customer ownership)" {
    $headers = @{ Authorization = "Bearer $script:customerToken" }
    $r = Invoke-RestMethod -Uri "$base/api/orders/$($script:orderId)" -Method GET -Headers $headers
    if ($r.orderId -ne $script:orderId) { throw "Wrong order returned" }
}

# 8. Public quote
Test-Step "POST /api/orders/public-quote" {
    $body = @{
        customerName = "Quote Lead"
        customerEmail = "quote@test.com"
        customerPhone = "9123456789"
        deliveryAddress = "Quote Address"
        items = @(@{ productId = $script:productId; quantity = 500 })
    } | ConvertTo-Json -Depth 5
    Invoke-RestMethod -Uri "$base/api/orders/public-quote" -Method POST -Body $body -ContentType "application/json" | Out-Null
}

# 9. Register admin and verify UTR endpoint is protected
$adminUser = "ta$(Get-Random -Maximum 99999)"
Test-Step "Finance admin endpoint requires auth" {
    try {
        Invoke-RestMethod -Uri "$base/api/finance/payments/utr/verify/$($script:orderId)?approved=true" -Method POST -ErrorAction Stop | Out-Null
        throw "UTR verify should require admin auth"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -ne 401 -and $_.Exception.Response.StatusCode.value__ -ne 403) {
            throw "Expected 401/403, got $($_.Exception.Response.StatusCode.value__)"
        }
    }
}

Write-Host "`n=== Results: $passed passed, $failed failed ===`n"
if ($failed -gt 0) { exit 1 }
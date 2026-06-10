$ErrorActionPreference = "Stop"
$baseUrl = "http://localhost:9191"
$orderId = $args[0]
if (-not $orderId) { $orderId = "1145b618-365e-41fb-93fe-fe86d67adbc2" }

$username = $args[1]
if (-not $username) { $username = "superadmin" }
$password = $args[2]
if (-not $password) { $password = "Admin@123" }

Write-Host "Logging in as $username..."
$loginBody = @{ username = $username; password = $password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$baseUrl/api/auth/login" -ContentType "application/json" -Body $loginBody
$token = $login.token
if (-not $token) { throw "Login failed: no token returned" }
$headers = @{ Authorization = "Bearer $token" }

Write-Host "Generating invoice for $orderId..."
try {
    $generate = Invoke-WebRequest -Method Post -Uri "$baseUrl/api/finance/invoice/generate/$orderId" -Headers $headers -ContentType "application/json" -Body "{}"
    Write-Host "Generate status: $($generate.StatusCode)"
    Write-Host "Generate body: $($generate.Content)"
} catch {
    $resp = $_.Exception.Response
    if ($resp) {
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        Write-Host "Generate failed: $($reader.ReadToEnd())"
    } else {
        throw
    }
}

Write-Host "Downloading invoice for $orderId..."
try {
    $download = Invoke-WebRequest -Method Get -Uri "$baseUrl/api/finance/invoice/download/$orderId" -Headers $headers
    $outFile = Join-Path $PSScriptRoot "invoice-test-$orderId.pdf"
    [IO.File]::WriteAllBytes($outFile, $download.Content)
    Write-Host "Download status: $($download.StatusCode)"
    Write-Host "Saved PDF: $outFile ($($download.RawContentLength) bytes)"
    if ($download.RawContentLength -lt 500) { throw "Downloaded file is too small to be a valid PDF" }
} catch {
    $resp = $_.Exception.Response
    if ($resp) {
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        Write-Host "Download failed: $($reader.ReadToEnd())"
        exit 1
    } else {
        throw
    }
}

Write-Host "Invoice API test passed." -ForegroundColor Green
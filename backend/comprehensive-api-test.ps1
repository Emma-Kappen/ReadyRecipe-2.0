$backendUrl = "http://localhost:8080"
$testResults = @()

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Body,
        [int]$ExpectedStatus = 200
    )
    
    try {
        $splat = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = 5
        }
        if ($Body) {
            $splat['Body'] = ($Body | ConvertTo-Json)
            $splat['ContentType'] = 'application/json'
        }
        
        $response = Invoke-RestMethod @splat
        $status = "PASS"
        $result = "Success (HTTP 200): $($response | ConvertTo-Json -Depth 3 -Compress)"
    } catch {
        $status = "FAIL"
        $result = $_.Exception.Message
    }
    
    $testResults += [PSCustomObject]@{
        Name = $Name
        Status = $status
        Details = $result
    }
    
    Write-Host "[$status] $Name"
}

Write-Host "===== READYRECIPE API TEST SUITE =====" -ForegroundColor Cyan
Write-Host "Backend: $backendUrl`n"

# ===== RECIPE ENDPOINTS =====
Write-Host "`n--- Recipe Endpoints ---" -ForegroundColor Yellow
Test-Endpoint -Name "GET /api/recipes (all)" `
    -Method GET `
    -Url "$backendUrl/api/recipes"

Test-Endpoint -Name "GET /api/recipes?cuisine=Italian" `
    -Method GET `
    -Url "$backendUrl/api/recipes?cuisine=Italian"

Test-Endpoint -Name "GET /api/recipes?cuisine=Indian" `
    -Method GET `
    -Url "$backendUrl/api/recipes?cuisine=Indian"

Test-Endpoint -Name "GET /api/recipes?cuisine=Chinese" `
    -Method GET `
    -Url "$backendUrl/api/recipes?cuisine=Chinese"

# ===== AUTH ENDPOINTS =====
Write-Host "`n--- Auth Endpoints ---" -ForegroundColor Yellow
Test-Endpoint -Name "POST /api/login (test1@example.com)" `
    -Method POST `
    -Url "$backendUrl/api/login" `
    -Body @{ email='test1@example.com'; password='testpass' }

Test-Endpoint -Name "POST /api/signup (new test user)" `
    -Method POST `
    -Url "$backendUrl/api/signup" `
    -Body @{ email='testapi@example.com'; password='apipass123' }

# ===== PANTRY ENDPOINTS (using known user IDs) =====
Write-Host "`n--- Pantry Endpoints ---" -ForegroundColor Yellow

# User 1: fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef
Test-Endpoint -Name "GET /api/pantry for user1" `
    -Method GET `
    -Url "$backendUrl/api/pantry?userId=fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef"

Test-Endpoint -Name "GET /api/pantry/stats for user1" `
    -Method GET `
    -Url "$backendUrl/api/pantry/stats?userId=fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef"

# User 2: 64a0c288-49e7-4931-a940-d3d0d0326be3 (test1@example.com)
Test-Endpoint -Name "GET /api/pantry for test1@example.com" `
    -Method GET `
    -Url "$backendUrl/api/pantry?userId=64a0c288-49e7-4931-a940-d3d0d0326be3"

Test-Endpoint -Name "GET /api/pantry/stats for test1@example.com" `
    -Method GET `
    -Url "$backendUrl/api/pantry/stats?userId=64a0c288-49e7-4931-a940-d3d0d0326be3"

# ===== SUMMARY =====
Write-Host "`n===== TEST SUMMARY =====" -ForegroundColor Cyan
$passed = ($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$failed = ($testResults | Where-Object { $_.Status -eq "FAIL" }).Count
$total = $testResults.Count

Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Passed: $passed" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { "Red" } else { "Green" })

Write-Host "`n--- Detailed Results ---" -ForegroundColor Yellow
$testResults | Format-Table -AutoSize -Wrap @(
    @{ Label = "Endpoint"; Expression = { $_.Name }; Width = 50 },
    @{ Label = "Status"; Expression = { $_.Status }; Width = 8 },
    @{ Label = "Details"; Expression = { $_.Details }; Width = 100 }
)

if ($failed -eq 0) {
    Write-Host "`n[PASS] ALL TESTS PASSED!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n[FAIL] Some tests failed." -ForegroundColor Red
    exit 1
}

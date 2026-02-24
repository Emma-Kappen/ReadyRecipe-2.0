$max=30
$i=0
while($i -lt $max) {
    try {
        $r = Invoke-RestMethod -Uri 'http://localhost:8080/api/recipes' -Method GET -TimeoutSec 5
        Write-Host '=== /api/recipes response ==='
        $r | ConvertTo-Json -Depth 4
        break
    } catch {
        Write-Host "Waiting for backend to be ready... attempt $($i+1)"
        Start-Sleep -Seconds 2
        $i++
    }
}
if($i -ge $max) { Write-Error 'Backend did not become ready in time.'; exit 2 }

Write-Host "Calling /api/pantry for user fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef"
try {
    $p = Invoke-RestMethod -Uri "http://localhost:8080/api/pantry?userId=fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef" -Method GET -TimeoutSec 5
    $p | ConvertTo-Json -Depth 5
} catch {
    Write-Host 'pantry call failed'
    Write-Host $_.Exception.Message
}

Write-Host "Calling /api/pantry/stats for same user"
try {
    $s = Invoke-RestMethod -Uri "http://localhost:8080/api/pantry/stats?userId=fd7fa497-a3a7-4622-a0a3-1d0e9cb4f8ef" -Method GET -TimeoutSec 5
    $s | ConvertTo-Json -Depth 5
} catch {
    Write-Host 'stats call failed'
    Write-Host $_.Exception.Message
}

Write-Host "Attempting POST /api/login for test1@example.com"
try {
    $body = @{ email='test1@example.com'; password='testpass' } | ConvertTo-Json
    $login = Invoke-RestMethod -Uri 'http://localhost:8080/api/login' -Method POST -Body $body -ContentType 'application/json' -TimeoutSec 10
    $login | ConvertTo-Json -Depth 5
} catch {
    Write-Host 'login call failed'
    Write-Host $_.Exception.Message
}

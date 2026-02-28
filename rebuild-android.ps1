param(
    [switch]$Clean
)

$ErrorActionPreference = 'Stop'

function Get-GradleVersionFromWrapper {
    param([string]$WrapperFile)
    $content = Get-Content -Path $WrapperFile -ErrorAction Stop
    $line = $content | Where-Object { $_ -match '^distributionUrl=' } | Select-Object -First 1
    if (-not $line) {
        throw "distributionUrl not found in $WrapperFile"
    }
    if ($line -match 'gradle-([0-9]+(?:\.[0-9]+){1,3})-') {
        return $Matches[1]
    }
    throw "Could not parse Gradle version from: $line"
}

function Get-JavaMajor {
    $output = (& cmd /c "java -version 2>&1") | Out-String
    $firstLine = ($output -split "`r?`n" | Where-Object { $_.Trim().Length -gt 0 } | Select-Object -First 1)
    if (-not $firstLine) {
        throw "Unable to read java -version output."
    }

    if ($firstLine -match 'version "(?<ver>[0-9]+)(?:\.[0-9]+)*') {
        return [int]$Matches['ver']
    }

    throw "Unable to parse Java version from: $firstLine"
}

function Stop-GradleAndLockers {
    param([string]$ModulePath, [string]$IsolatedHome)

    Push-Location $ModulePath
    try {
        Write-Host "Stopping Gradle daemons via wrapper..."
        & .\gradlew.bat --stop | Out-Host
    }
    finally {
        Pop-Location
    }

    $candidates = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe' OR Name='gradle.exe'" -ErrorAction SilentlyContinue
    if (-not $candidates) {
        Write-Host "No java/gradle processes found after daemon stop."
        return
    }

    $killed = @()
    foreach ($proc in $candidates) {
        $cmd = [string]$proc.CommandLine
        if ([string]::IsNullOrWhiteSpace($cmd)) {
            continue
        }

        $holdsGradleCache =
            ($cmd -match 'org\.gradle\.launcher\.daemon') -or
            ($cmd -match 'GradleDaemon') -or
            ($cmd -like "*$IsolatedHome*") -or
            ($cmd -like "*$env:USERPROFILE\\.gradle*")

        if ($holdsGradleCache) {
            try {
                Stop-Process -Id $proc.ProcessId -Force -ErrorAction Stop
                $killed += [pscustomobject]@{
                    Id = $proc.ProcessId
                    Name = $proc.Name
                }
            }
            catch {
                Write-Warning "Could not stop process $($proc.ProcessId): $($_.Exception.Message)"
            }
        }
    }

    if ($killed.Count -gt 0) {
        Write-Host "Stopped potential cache-locking processes:"
        $killed | Format-Table -AutoSize | Out-Host
    }
    else {
        Write-Host "No cache-locking java/gradle processes detected."
    }
}

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$moduleName = 'android'
$modulePath = Join-Path $repoRoot $moduleName
$wrapperFile = Join-Path $modulePath 'gradle\wrapper\gradle-wrapper.properties'

if (-not (Test-Path $wrapperFile)) {
    throw "Wrapper file not found: $wrapperFile"
}

Write-Host "=== Android Deterministic Rebuild ==="
Write-Host "Module path: $modulePath"
Write-Host "Requested clean cache reset: $Clean"

$javaMajor = Get-JavaMajor
& cmd /c "java -version 2>&1" | Out-Host
Write-Host "JAVA_HOME=$env:JAVA_HOME"
if ($javaMajor -ne 17) {
    throw "Java 17 is required for this workflow. Detected major version: $javaMajor"
}

Write-Host "Initial GRADLE_USER_HOME=$env:GRADLE_USER_HOME"

$gradleVersion = Get-GradleVersionFromWrapper -WrapperFile $wrapperFile
$isolatedHome = Join-Path $env:USERPROFILE ".gradle-$moduleName-$gradleVersion"

Stop-GradleAndLockers -ModulePath $modulePath -IsolatedHome $isolatedHome

$env:GRADLE_USER_HOME = $isolatedHome
Write-Host "Active GRADLE_USER_HOME=$env:GRADLE_USER_HOME"

if ($Clean) {
    Write-Host "Cleaning isolated cache at $isolatedHome"
    if (Test-Path $isolatedHome) {
        Remove-Item -Path $isolatedHome -Recurse -Force -ErrorAction SilentlyContinue
    }
}

if (-not (Test-Path $isolatedHome)) {
    New-Item -ItemType Directory -Path $isolatedHome -Force | Out-Null
}

Push-Location $modulePath
try {
    & .\gradlew.bat --no-daemon --refresh-dependencies clean build
    $exitCode = $LASTEXITCODE
}
finally {
    Pop-Location
}

if ($exitCode -ne 0) {
    Write-Host "=== FAILURE: Android build failed (exit code $exitCode) ===" -ForegroundColor Red
    exit $exitCode
}

Write-Host "=== SUCCESS: Android build completed ===" -ForegroundColor Green
exit 0

param(
    [switch]$KeepTemp,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs
)

$ErrorActionPreference = "Stop"

if (-not $MavenArgs -or $MavenArgs.Count -eq 0) {
    $MavenArgs = @("test")
}

$runRoot = Join-Path $env:TEMP ("library-maven-" + [guid]::NewGuid().ToString("N").Substring(0, 8))
$repoLink = Join-Path $runRoot "repo"
$mavenHome = Join-Path $env:USERPROFILE ".m2"
$mavenRepo = Join-Path $mavenHome "repository"

New-Item -ItemType Directory -Path $runRoot | Out-Null
New-Item -ItemType Directory -Path $mavenRepo -Force | Out-Null

try {
    New-Item -ItemType Junction -Path $repoLink -Target $PSScriptRoot | Out-Null
    $env:MAVEN_USER_HOME = $mavenHome

    Push-Location $repoLink
    try {
        & ".\mvnw.cmd" "-Dmaven.repo.local=$mavenRepo" @MavenArgs
        exit $LASTEXITCODE
    } finally {
        Pop-Location
    }
} finally {
    if ($KeepTemp) {
        Write-Host "Kept temp Maven workspace at: $runRoot"
        Write-Host "Maven cache: $mavenHome"
    } else {
        if (Test-Path $runRoot) {
            Remove-Item $runRoot -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

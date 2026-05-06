param(
    [string]$Port = "8081"
)

$ErrorActionPreference = "Stop"

Remove-Item Env:\SPRING_DATASOURCE_URL -ErrorAction SilentlyContinue

& "$PSScriptRoot\run-maven-ascii.ps1" spring-boot:run "-Dspring-boot.run.arguments=--server.port=$Port"

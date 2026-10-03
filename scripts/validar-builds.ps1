$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

$Projects = @(
    "config-server",
    "discovery-server",
    "auth-server",
    "core-banco",
    "bff-web",
    "bff-mobile",
    "bff-atm"
)

foreach ($Project in $Projects) {
    Write-Host "`n=== VALIDANDO $Project ===" -ForegroundColor Cyan
    Push-Location (Join-Path $Root $Project)
    try {
        & .\mvnw.cmd test
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo Maven en $Project"
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host "`nVALIDACION SEMANA 8 COMPLETA: TODOS LOS MICROSERVICIOS -> BUILD SUCCESS" -ForegroundColor Green

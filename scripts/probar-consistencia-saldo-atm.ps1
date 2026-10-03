param(
    [int]$CuentaId = 101,
    [decimal]$Monto = 1000
)

$ErrorActionPreference = "Stop"

function Obtener-Token([string]$Scope) {
    $respuesta = curl.exe -s `
        -u "banco-service:banco-service-secret" `
        -X POST "http://localhost:9000/oauth2/token" `
        -H "Content-Type: application/x-www-form-urlencoded" `
        -d "grant_type=client_credentials&scope=$Scope"

    $json = $respuesta | ConvertFrom-Json
    if (-not $json.access_token) {
        throw "No fue posible obtener token OAuth2 para el scope $Scope. Respuesta: $respuesta"
    }

    return $json.access_token
}

Write-Host "=== Validacion de consistencia de saldo ATM por evento Kafka ===" -ForegroundColor Cyan
Write-Host "Cuenta: $CuentaId | Monto retiro: $Monto"

$tokenRead = Obtener-Token "banco.read"
$tokenWrite = Obtener-Token "banco.write"

$saldoAntesRaw = curl.exe -k -s `
    -H "Authorization: Bearer $tokenRead" `
    "https://localhost:8083/api/atm/cuentas/$CuentaId/saldo"
$saldoAntes = $saldoAntesRaw | ConvertFrom-Json

if ($null -eq $saldoAntes.saldo) {
    throw "No se pudo consultar el saldo inicial. Respuesta: $saldoAntesRaw"
}

Write-Host "Saldo consultado antes del retiro: $($saldoAntes.saldo)"

if ([decimal]$saldoAntes.saldo -lt $Monto) {
    throw "El saldo inicial no alcanza para demostrar una actualizacion por retiro APROBADO. Use un monto menor."
}

$archivoTemporal = Join-Path $PSScriptRoot "retiro-consistencia-temp.json"

try {
    Set-Content -Path $archivoTemporal -Value ("{`"monto`":" + $Monto.ToString([System.Globalization.CultureInfo]::InvariantCulture) + "}") -Encoding ASCII

    $retiroRaw = curl.exe -k -s `
        -H "Authorization: Bearer $tokenWrite" `
        -H "Content-Type: application/json" `
        --data-binary "@$archivoTemporal" `
        "https://localhost:8083/api/atm/cuentas/$CuentaId/retiros"
    $retiro = $retiroRaw | ConvertFrom-Json

    if (-not $retiro.transaccionId) {
        throw "No se recibio transaccionId. Respuesta: $retiroRaw"
    }

    Write-Host "Retiro aceptado. Transaccion: $($retiro.transaccionId) | Estado inicial: $($retiro.estado)"

    $resultado = $null
    for ($i = 1; $i -le 15; $i++) {
        Start-Sleep -Seconds 1

        $estadoRaw = curl.exe -k -s `
            -H "Authorization: Bearer $tokenRead" `
            "https://localhost:8083/api/atm/retiros/$($retiro.transaccionId)"
        $resultado = $estadoRaw | ConvertFrom-Json

        if ($resultado.estado -and $resultado.estado -ne "PENDIENTE") {
            break
        }
    }

    if (-not $resultado -or $resultado.estado -eq "PENDIENTE") {
        throw "El retiro no alcanzo un estado final dentro del tiempo de espera."
    }

    Write-Host "Resultado Kafka: $($resultado.estado) | Saldo anterior: $($resultado.saldoAnterior) | Saldo final: $($resultado.saldoFinal)"

    $saldoDespuesRaw = curl.exe -k -s `
        -H "Authorization: Bearer $tokenRead" `
        "https://localhost:8083/api/atm/cuentas/$CuentaId/saldo"
    $saldoDespues = $saldoDespuesRaw | ConvertFrom-Json

    Write-Host "Saldo consultado despues del evento: $($saldoDespues.saldo)"

    if ([decimal]$saldoDespues.saldo -ne [decimal]$resultado.saldoFinal) {
        throw "CONSISTENCIA FALLIDA: ATM informa $($saldoDespues.saldo), pero Kafka informo saldoFinal=$($resultado.saldoFinal)."
    }

    Write-Host "CONSISTENCIA OK: el BFF ATM actualizo su saldo en memoria con saldoFinal recibido desde Kafka." -ForegroundColor Green
}
finally {
    Remove-Item -Path $archivoTemporal -ErrorAction SilentlyContinue
}

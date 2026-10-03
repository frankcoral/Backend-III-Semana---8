# Banco XYZ — Desarrollo Backend III — Semana 8

Proyecto final de la Semana 8 de **Desarrollo Backend III (PBY2203)**. La solución consolida el caso Banco XYZ en una arquitectura de microservicios segura, resiliente, orientada a eventos y desplegable mediante Docker Compose.

## Objetivo

Integrar en una sola solución:

- **Spring Cloud Config** para configuración centralizada.
- **Eureka Service Discovery** para registro y descubrimiento de servicios.
- **Spring Cloud LoadBalancer** para resolver dinámicamente `core-banco` desde los BFF.
- **OAuth 2.0 + JWT** con Spring Authorization Server.
- **Spring Security Resource Server** en Core Banco y los tres BFF.
- **Resilience4j Circuit Breaker** con fallback y recuperación.
- **Apache Kafka** para retiros ATM asíncronos.
- **Docker + Docker Compose** para contenerización y orquestación integral.

## Arquitectura

```text
                         +----------------------+
                         |     auth-server      |
                         | OAuth2 / JWT :9000   |
                         +----------+-----------+
                                    |
                                    | JWT
                                    v

+-------------+       +-------------+       +-------------+
|   bff-web   |       | bff-mobile  |       |   bff-atm   |
| HTTPS :8081 |       | HTTPS :8082 |       | HTTPS :8083 |
| Resilience4j|       | Resilience4j|       | Resilience4j|
+------+------+       +------+------+       +------+------+
       \                     |                     /
        \                    |                    /
         +-------------------+-------------------+
                             |
                  Eureka + LoadBalancer
                             |
                             v
                      +-------------+
                      | core-banco  |
                      |    :8080    |
                      +------+------+
                             |
                             | eventos
                             v
              +-----------------------------+
              | Apache Kafka :9092          |
              | retiros.solicitados         |
              | retiros.resultados          |
              | retiros.dlt                 |
              +-----------------------------+

+----------------------+       +----------------------+
|    config-server     |       |   discovery-server   |
|        :8888         |       | Eureka :8761         |
+----------------------+       +----------------------+
```

## Componentes y puertos

| Componente | Puerto | Función |
|---|---:|---|
| `config-server` | 8888 | Configuración centralizada |
| `discovery-server` | 8761 | Eureka Service Discovery |
| `auth-server` | 9000 | Authorization Server OAuth2 / JWT |
| `core-banco` | 8080 | Lógica central y datos legacy |
| `bff-web` | 8081 HTTPS | Respuesta completa para canal Web |
| `bff-mobile` | 8082 HTTPS | Respuesta optimizada para Mobile |
| `bff-atm` | 8083 HTTPS | Saldo, retiros y estado de transacciones |
| `kafka` | 9092 | Mensajería asíncrona |

## Estructura del proyecto

```text
banco-xyz-bff/
├── auth-server/
├── bff-atm/
├── bff-mobile/
├── bff-web/
├── config-server/
├── core-banco/
├── discovery-server/
├── docs/
│   └── Propuesta Técnica Semana 8.docx
├── evidencias/
│   ├── README.md
│   └── 01 ... 23 .png
├── scripts/
│   ├── generar-certificados.ps1
│   └── validar-builds.ps1
├── docker-compose.yml
└── README.md
```

## Requisitos

- Docker Desktop.
- Docker Compose.
- Git.
- PowerShell para los comandos de ejemplo.
- Puertos `8080`, `8081`, `8082`, `8083`, `8761`, `8888`, `9000` y `9092` disponibles.

Los BFF incluyen un certificado PKCS12 autofirmado de laboratorio para HTTPS. Por eso los ejemplos con `curl.exe` usan `-k`.

## Ejecución con Docker Compose

Desde la raíz del proyecto:

```powershell
docker compose up -d --build
```

Comprobar el estado:

```powershell
docker compose ps
```

Deben quedar activos los servicios principales:

```text
banco-config-server
banco-discovery-server
banco-auth-server
banco-core-banco
banco-bff-web
banco-bff-mobile
banco-bff-atm
week8-kafka
```

`week8-kafka-init` es un contenedor auxiliar: crea los tópicos y termina después de completar esa tarea.

Para detener el entorno:

```powershell
docker compose down
```

## Service Discovery + LoadBalancer

Panel Eureka:

```text
http://localhost:8761
```

Servicios registrados:

```text
AUTH-SERVER
CORE-BANCO
BFF-WEB
BFF-MOBILE
BFF-ATM
```

Los BFF acceden al Core usando el nombre lógico:

```text
http://core-banco
```

La resolución se realiza mediante **Eureka + Spring Cloud LoadBalancer**, evitando una URL fija del Core y aplicando la mejora indicada en la retroalimentación de la semana anterior.

## OAuth 2.0 y JWT

`auth-server` emite JWT. `core-banco` y los BFF funcionan como OAuth2 Resource Servers.

Scopes utilizados:

```text
banco.read
banco.write
```

### Token de lectura

```powershell
$tokenRead = (curl.exe -s `
  -u "banco-service:banco-service-secret" `
  -X POST "http://localhost:9000/oauth2/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=client_credentials&scope=banco.read" | ConvertFrom-Json).access_token
```

### Token de escritura

```powershell
$tokenWrite = (curl.exe -s `
  -u "banco-service:banco-service-secret" `
  -X POST "http://localhost:9000/oauth2/token" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "grant_type=client_credentials&scope=banco.write" | ConvertFrom-Json).access_token
```

### Ejemplo BFF Web protegido

```powershell
curl.exe -k -i `
  -H "Authorization: Bearer $tokenRead" `
  https://localhost:8081/api/web/dashboard
```

La evidencia demuestra:

- petición sin token → `401`;
- token con `banco.read` → `200`;
- token sin el scope requerido → `403`;
- retiro ATM con `banco.write` → `202`.

## Resilience4j

Los BFF usan **Circuit Breaker** para las llamadas hacia `core-banco`.

Prueba evidenciada:

1. Con Core disponible, el BFF Web responde con datos reales.
2. Se detiene `banco-core-banco`.
3. El BFF Web sigue respondiendo mediante `FALLBACK ACTIVO`.
4. Se inicia nuevamente Core Banco.
5. El circuito se recupera y el BFF vuelve a entregar los datos reales.

La guía también presenta Retry Policies y Bulkheads. En este proyecto, el mecanismo implementado y evidenciado para el criterio de tolerancia a fallos es **Circuit Breaker con fallback**.

## Kafka y arquitectura orientada a eventos

Flujo de retiro ATM:

```text
Cliente ATM
   |
   | POST retiro
   v
bff-atm
   |
   | RETIRO_SOLICITADO
   v
banco.retiros.solicitados
   |
   v
core-banco
   |
   | RETIRO_APROBADO / RETIRO_RECHAZADO
   v
banco.retiros.resultados
   |
   v
bff-atm
   |
   v
Consulta del estado final
```

Tópicos configurados:

```text
banco.retiros.solicitados
banco.retiros.resultados
banco.retiros.dlt
```

Los tópicos se crean con **3 particiones**. `cuentaId` se utiliza como clave de particionamiento para conservar el orden de eventos de una misma cuenta y permitir procesamiento paralelo entre cuentas distintas.

### Consistencia del saldo ATM después de un retiro

A partir de la retroalimentación de la Semana 7, el saldo en memoria del BFF ATM también se sincroniza cuando llega un evento por `banco.retiros.resultados`. El `saldoFinal` calculado por `core-banco` se considera la referencia autoritativa y reemplaza el valor previamente almacenado en la caché del ATM.

Esto evita el caso en que el ATM consulte primero un saldo, procese después un retiro aprobado y continúe respondiendo con el saldo anterior. La actualización se realiza tanto para resultados aprobados como rechazados siempre que el evento incluya `saldoFinal`.

La responsabilidad se separó en `SaldoCuentaService`, utilizado por `AtmService` para consultar la caché y por `RetiroResultadoConsumer` para actualizarla desde Kafka.

Prueba automática de integración:

```powershell
.\scripts\probar-consistencia-saldo-atm.ps1
```

La prueba consulta primero el saldo para dejarlo almacenado en memoria, realiza un retiro de `$1000`, espera el resultado Kafka y vuelve a consultar el saldo. El resultado es correcto cuando el saldo informado por el ATM coincide con `saldoFinal` del evento.

### Listar tópicos

```powershell
docker exec week8-kafka /opt/kafka/bin/kafka-topics.sh `
  --bootstrap-server localhost:9092 `
  --list
```

### Verificar las 3 particiones

```powershell
docker exec week8-kafka /opt/kafka/bin/kafka-topics.sh `
  --bootstrap-server localhost:9092 `
  --describe `
  --topic banco.retiros.solicitados
```

## Prueba de retiro ATM

Crear la solicitud:

```powershell
Set-Content -Path .\retiro.json -Value '{"monto":15000}' -Encoding ASCII
```

Enviar el retiro:

```powershell
curl.exe -k -i `
  -H "Authorization: Bearer $tokenWrite" `
  -H "Content-Type: application/json" `
  --data-binary "@retiro.json" `
  https://localhost:8083/api/atm/cuentas/101/retiros
```

La respuesta inicial es `HTTP 202` con un `transaccionId`, porque el retiro continúa mediante Kafka.

Consultar el resultado:

```powershell
curl.exe -k -i `
  -H "Authorization: Bearer $tokenRead" `
  https://localhost:8083/api/atm/retiros/<TRANSACCION_ID>
```

En la prueba final documentada, el retiro de `$15000` sobre la cuenta `101` terminó como:

```text
estado: RECHAZADO
motivo: SALDO_INSUFICIENTE
saldoAnterior: 8000
saldoFinal: 8000
```

`retiro.json` es temporal y está excluido mediante `.gitignore`.

## Config Server

Comprobación:

```powershell
curl.exe -i http://localhost:8888/bff-atm/default
```

Los BFF importan configuración desde Config Server mediante `spring.config.import`.

## Validación de builds

El script:

```text
scripts/validar-builds.ps1
```

recorre los siete microservicios propios y ejecuta sus pruebas Maven.

## Evidencias

El índice completo y la relación de cada captura con la pauta están en:

```text
evidencias/README.md
```

La carpeta contiene **24 evidencias reales de Semana 8**, incluyendo OAuth2, Docker, Docker Compose, Eureka, LoadBalancer, Kafka, retiro asíncrono y Resilience4j.


## Propuesta técnica

Documento incluido en:

```text
docs/Propuesta_Tecnica_Semana8_Banco_XYZ.docx
```

## Correspondencia con la pauta

| Criterio | Implementación / evidencia |
|---|---|
| OAuth2.0 | Authorization Server, JWT, scopes y respuestas 401/200/403 |
| Imágenes Docker | Dockerfile en los siete microservicios propios |
| Docker Compose | Config, Eureka, Auth, Core, 3 BFF y Kafka en una red común |
| Resilience4j | Circuit Breaker, fallback y recuperación |
| Kafka/JMS | Kafka asíncrono, tres tópicos, tres particiones y actualización del saldo ATM desde eventos de resultado |
| Entrega | Código fuente, README, propuesta técnica y 24 evidencias |

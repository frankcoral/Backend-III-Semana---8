# Evidencias — Desarrollo Backend III — Semana 8

Esta carpeta contiene las **24 evidencias reales** obtenidas durante la implementación y validación del proyecto Banco XYZ de la Semana 8. Las capturas están numeradas en el orden en que se comprobó la solución.

## Índice de evidencias

| N° | Archivo | Qué demuestra | Criterio principal |
|---:|---|---|---|
| 01 | `01-oauth2-token-client-credentials.png` | Emisión de un access token OAuth2 mediante `client_credentials`. | OAuth2.0 |
| 02 | `02-oauth2-core-sin-token-401.png` | Core Banco rechaza una solicitud protegida sin JWT con HTTP 401. | OAuth2.0 |
| 03 | `03-oauth2-core-token-valido-200.png` | Core Banco acepta un JWT válido con `banco.read` y responde HTTP 200. | OAuth2.0 |
| 04 | `04-oauth2-core-scope-incorrecto-403.png` | Un token sin el scope requerido recibe HTTP 403. | OAuth2.0 |
| 05 | `05-oauth2-bff-web-core-loadbalancer-200.png` | BFF Web protegido consulta Core Banco usando Eureka + Spring Cloud LoadBalancer. | OAuth2 / Spring Cloud |
| 06 | `06-oauth2-bff-mobile-core-loadbalancer-200.png` | BFF Mobile protegido consulta Core Banco mediante resolución dinámica. | OAuth2 / Spring Cloud |
| 07 | `07-oauth2-bff-atm-saldo-read-200.png` | BFF ATM permite consulta de saldo con scope de lectura. | OAuth2.0 |
| 08 | `08-oauth2-bff-atm-retiro-write-202.png` | BFF ATM acepta un retiro con scope de escritura y responde HTTP 202. | OAuth2 / Kafka |
| 09 | `09-kafka-retiro-solicitado.png` | Evento `RETIRO_SOLICITADO` publicado en `banco.retiros.solicitados`. | Kafka |
| 10 | `10-kafka-retiro-rechazado-saldo-insuficiente.png` | Core publica `RETIRO_RECHAZADO` por saldo insuficiente en el topic de resultados. | Kafka |
| 11 | `11-bff-atm-estado-retiro-rechazado-200.png` | BFF ATM expone el estado final procesado de la transacción. | Kafka / BFF ATM |
| 12 | `12-config-server-bff-atm-200.png` | Config Server entrega la configuración de `bff-atm`. | Spring Cloud Config |
| 13 | `13-docker-config-server-200.png` | Config Server ejecutándose correctamente desde su imagen Docker. | Docker |
| 14 | `14-docker-eureka-up.png` | Eureka ejecutándose dentro de Docker y respondiendo correctamente. | Docker / Eureka |
| 15 | `15-docker-compose-stack-up.png` | `docker compose ps` muestra el stack principal completo en estado `Up`. | Docker Compose |
| 16 | `16-eureka-todos-los-servicios-up.png` | Eureka muestra `AUTH-SERVER`, `CORE-BANCO`, `BFF-WEB`, `BFF-MOBILE` y `BFF-ATM` en estado `UP`. | Eureka / Docker Compose |
| 17 | `17-docker-compose-oauth2-bff-web-200.png` | Dentro del stack final, OAuth2 + BFF Web + Core Banco responden correctamente. | OAuth2 / Docker Compose |
| 18 | `18-docker-compose-kafka-topics.png` | Kafka en Compose contiene los tópicos solicitados, resultados y DLT. | Kafka / Docker Compose |
| 19 | `19-docker-compose-atm-retiro-202.png` | Retiro ATM iniciado dentro del stack Docker Compose y aceptado de forma asíncrona. | OAuth2 / Kafka / Compose |
| 20 | `20-docker-compose-atm-resultado-rechazado.png` | El retiro anterior llega a estado `RECHAZADO` por `SALDO_INSUFICIENTE`. | Kafka end-to-end |
| 21 | `21-resilience4j-fallback-core-apagado.png` | Con Core Banco detenido, BFF Web entrega `FALLBACK ACTIVO`. | Resilience4j |
| 22 | `22-resilience4j-recuperacion-core-200.png` | Tras reactivar Core Banco, el BFF vuelve a entregar datos reales con HTTP 200. | Resilience4j |
| 23 | `23-kafka-topic-3-particiones.png` | `banco.retiros.solicitados` posee tres particiones para procesamiento concurrente. | Kafka / escalabilidad |
| 24 | `24-consistencia-saldo-atm-kafka.png` | Tras un retiro aprobado de 1000, el BFF ATM actualiza el saldo en memoria de 8000 a 7000 usando `saldoFinal` recibido desde Kafka. | Kafka / consistencia de datos |

## Relación con la pauta de evaluación

### 1. OAuth2.0 — 20 puntos

Evidencias principales: **01, 02, 03, 04, 07, 08 y 17**.

Se demuestra emisión de JWT, protección de recursos, autorización por scopes y uso real del token a través de los BFF.

### 2. Imágenes Docker funcionales — 20 puntos

Evidencias principales: **13, 14 y 15**.

Además, el proyecto contiene `Dockerfile` para:

```text
config-server
discovery-server
auth-server
core-banco
bff-web
bff-mobile
bff-atm
```

La evidencia 15 muestra el resultado de levantar las imágenes en el stack completo.

### 3. Docker Compose — 20 puntos

Evidencias principales: **15, 16, 17, 18, 19 y 20**.

Se demuestra la orquestación conjunta de Config Server, Eureka, Authorization Server, Core Banco, tres BFF y Kafka.

### 4. Resilience4j — 20 puntos

Evidencias principales: **21 y 22**.

La evidencia 21 muestra el fallback cuando Core Banco no está disponible. La evidencia 22 demuestra la recuperación posterior.

### 5. Kafka/JMS — 15 puntos

Evidencias principales: **08, 09, 10, 11, 18, 19, 20, 23 y 24**.

Se demuestra el flujo asíncrono completo de retiros, los topics requeridos, la configuración de tres particiones y la sincronización del saldo ATM con `saldoFinal` recibido desde Kafka.

### 6. Código, documentación y evidencia — 5 puntos

Este proyecto incluye:

```text
README.md
Dockerfiles
docker-compose.yml
docs/Propuesta Técnica Semana 8.docx
evidencias/README.md
evidencias/01...24.png
```

## Secuencia recomendada para revisar las capturas

La secuencia más directa para demostrar el funcionamiento integral es:

```text
15 -> 16 -> 17 -> 18 -> 19 -> 20 -> 21 -> 22 -> 23 -> 24
```

Con esas diez capturas se observa el stack completo, descubrimiento de servicios, seguridad OAuth2, Kafka end-to-end, resiliencia, escalabilidad y consistencia del saldo ATM. Las evidencias 01 a 14 entregan trazabilidad adicional de cada componente durante su implementación.

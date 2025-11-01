## DeliveryCo Service

```
http://localhost:8081
```

DeliveryCo is a Spring Boot microservice responsible for orchestrating the lifecycle of parcel deliveries. It consumes delivery requests from the Store application, schedules asynchronous status transitions, and publishes the resulting state changes to Kafka so that downstream systems (Store, EmailService) can react.

### Architecture Overview

- **Inbound integration**: Kafka consumer (or REST fallback) receives delivery requests and persists them as `DeliveryOrder` aggregates.
- **Persistence**: PostgreSQL accessed through Spring Data JPA; Flyway is used for schema migrations. Entities mirror the ERD (`DeliveryOrder`, `DeliveryItem`, `DeliveryStatusEvent`, `DeliveryJob`, `DeliveryOutbox`, `DeliveryIncident`, `DeliveryWorkerHeartbeat`).
- **Asynchronous scheduling**: `DeliveryJob` records act as durable timers. Worker components claim and execute jobs, updating order status and creating new jobs as needed.
- **Outbox pattern**: Status events destined for Kafka are first written to `DeliveryOutbox`, then a dedicated publisher reliably emits them to the broker with retries and dead-letter handling.
- **Observability**: Spring Boot Actuator, correlation IDs, and incident logging provide diagnostics. Heartbeats allow operators to detect failed workers.

### Quality Attributes

- **Availability & reliability**: Durable jobs/outbox guarantee progress even if API, worker, or publisher nodes fail mid-operation. Optimistic locking and heartbeats coordinate multiple instances without single points of failure.
- **Scalability**: Stateless API tier plus horizontally scalable job/publisher workers. Kafka partitions and job sharding allow linear scaling.
- **Modifiability & maintainability**: Clear separation between domain services, messaging adapters, and persistence via Spring's layered architecture. Domain logic avoids dependence on Kafka/Postgres specifics.
- **Observability**: Structured events with `correlationId`, incidents, and actuator metrics support monitoring and tracing.
- **Security**: Sensitive addresses/emails remain in the persistence layer; configuration supports TLS/Postgres role separation.

### Module Layout

- `com.deliveryco.DeliveryCoApplication` – Spring Boot entry point.
- `com.deliveryco.config` – Infrastructure configuration (Kafka, Postgres, scheduling).
- `com.deliveryco.domain` – Domain services and models (order orchestration, loss simulation).
- `com.deliveryco.entity` – JPA entities defined from the ERD.
- `com.deliveryco.messaging` – Kafka consumers/producers and outbox publisher.
- `com.deliveryco.repository` – Spring Data interfaces.
- `com.deliveryco.web` – REST endpoints (health checks, admin tools).

### Next Steps

1. Configure PostgreSQL, Kafka, and pgAdmin (`docker-compose.yml` is provided) and update `application.yml` with credentials.
2. Implement Kafka topics and consumer/publisher configuration.
3. Expand integration tests using Testcontainers for Postgres and Kafka.

### Building & Running

- Install JDK 17 (e.g., `brew install temurin17`) and point `JAVA_HOME` to it (`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`).
- Install Gradle 8.x (or run `gradle wrapper` if you prefer to generate the Gradle wrapper locally).
- Build: `gradle clean build`
- Run: `gradle bootRun`

### End-to-end: run DeliveryCo + EmailService

1) Start infrastructure (Kafka, ZooKeeper, Postgres, pgAdmin)
- `cd delivery-co`
- `docker compose up -d`

2) Run DeliveryCo
- `cd delivery-co`
- `./gradlew bootRun`

3) Run EmailService (uses the same Postgres by default)
- `cd email_service`
- `gradle bootRun`

4) Open the demo inbox UI
- Go to `http://localhost:8081`
- Enter `demo@customer.local` and click “Load”

5) Trigger a delivery (new request shape with multiple pickups)
- `curl -X POST http://localhost:8080/api/deliveries -H 'Content-Type: application/json' -d '{
  "externalOrderId":"ORD-DEMO-UI",
  "customerId":"C-DEMO",
  "pickupLocations": { "WH-1":"1 Warehouse Way", "WH-2":"22 Secondary Rd" },
  "dropoffAddress":"22 Customer St",
  "contactEmail":"demo@customer.local",
  "lossRate":0.05,
  "items": { "WH-1": {"SKU-1":1}, "WH-2": {"SKU-2":3} }
}'`

You should see at most one email per status transition (RECEIVED, PICKED_UP, IN_TRANSIT, DELIVERED or LOST). Reloading still shows one per status because the database enforces de-duplication.

Notes:
- If you want a fresh inbox for demos, clear the `email_message` table or change the externalOrderId.
- If you run EmailService against H2 in-memory for a quick demo, add env overrides; Flyway V1/V2 also support H2.

## Integration Guide

DeliveryCo exposes both synchronous and asynchronous entry points so other services can request deliveries and track progress.

### Email Service + UI (How we wired it up)

We ship a separate `email_service` Spring Boot app that consumes `delivery.status` events and shows a tiny inbox UI (SSE + simple HTML) for demo purposes. Key implementation choices and fixes:

- Idempotent emails: DB-level unique index ensures one email per (to_address, external_order_id, message_type). Code also checks before insert.
- Contact email propagation: DeliveryCo includes `payload.contactEmail` in every status event so EmailService knows where to send.
- JSON type headers off: DeliveryCo producer disables `spring.json.add.type.headers` so cross-service deserialization works without class headers.
- Frontend live updates: SSE stream emits each email ID once; client de-dupes by ID to avoid duplicates on reconnect or re-order.

Files in email_service:
- `email_service/src/main/resources/db/migration/V1__create_email_tables.sql` – creates `email_message` table.
- `email_service/src/main/resources/db/migration/V2__add_unique_email_constraint.sql` – enforces one email per status per order (unique index).
- `email_service/src/main/resources/application.properties` – defaults to the same Postgres as DeliveryCo Compose (jdbc:postgresql://localhost:15432/deliveryco, user `deliveryco`, password `changeme`).
- `email_service/src/main/java/com/example/emailservice/messaging/DeliveryStatusListener.java` – normalizes addresses and de-dupes before saving.
- `email_service/src/main/java/com/example/emailservice/controller/EmailController.java` – REST APIs + SSE stream that only emits unseen email IDs.
- `email_service/src/main/resources/static/app.js` – client-side de-dupe and single EventSource instance.

### REST API

**Endpoint:** `POST /api/deliveries`

**Request body (`application/json`):**

```json
{
  "externalOrderId": "ORD-1234",
  "customerId": "C-1",
  "pickupLocations": {
    "WH-1": "1 Warehouse Way",
    "WH-2": "22 Secondary Rd"
  },
  "dropoffAddress": "77 Client Road",
  "contactEmail": "customer@example.com",
  "lossRate": 0.05,
  "items": {
    "WH-1": { "SKU-1": 2, "SKU-2": 1 },
    "WH-2": { "SKU-3": 5 }
  }
}
```

- `externalOrderId` must be unique per order (idempotent: resubmitting the same ID returns the existing order).
- `lossRate` is optional; defaults to `deliveryco.scheduler.loss-rate-default`.

**Response:**

```json
{
  "deliveryOrderId": "b9c4c950-8d58-4c6a-9daf-3fe83f8f8eb5",
  "externalOrderId": "ORD-1234",
  "status": "RECEIVED",
  "requestedAt": "2025-10-17T09:55:44.662145Z"
}
```

### Kafka Contracts

DeliveryCo supports asynchronous integration using Kafka topics defined in `application.yml`.

#### Inbound Delivery Requests

- **Topic:** `delivery.requests`
- **Key:** `externalOrderId`
- **Value:** `DeliveryRequestMessage`

```json
{
  "externalOrderId": "ORD-1234",
  "customerId": "C-1",
  "pickupLocations": {
    "WH-1": "1 Warehouse Way",
    "WH-2": "22 Secondary Rd"
  },
  "dropoffAddress": "77 Client Road",
  "contactEmail": "customer@example.com",
  "lossRate": 0.05,
  "items": {
    "WH-1": { "SKU-1": 2, "SKU-2": 1 },
    "WH-2": { "SKU-3": 5 }
  }
}
```

Empty payloads are ignored; malformed JSON is logged and skipped via Spring’s `ErrorHandlingDeserializer`.

#### Outbound Status Updates

- **Topic:** `delivery.status`
- **Key:** `externalOrderId`
- **Value:** `DeliveryStatusMessage`

```json
{
  "eventId": "81fe8112-70fa-48d5-9470-697150cdd283",
  "deliveryOrderId": "d31271c7-75c3-48e8-b49f-5f78bfb3e4b2",
  "externalOrderId": "ORD-1234",
  "correlationId": "d7f9f785-86d3-432b-b593-9fcacb973fcc",
  "status": "IN_TRANSIT",
  "reason": "Package out for delivery",
  "occurredAt": "2025-10-17T10:33:32.790Z",
  "payload": {}
}
```

`status` transitions through `RECEIVED → PICKED_UP → IN_TRANSIT → DELIVERED` (or `LOST`). Each change is guaranteed via the outbox publisher; failures are retried and optionally dead-lettered to `delivery.status.dlq`.

### Store Webhook (optional)

DeliveryCo can also notify the Store via REST on every status change. This is in addition to Kafka (EmailService still consumes Kafka).

- Config (application.yml)
  - `deliveryco.store-webhook.enabled`: true|false (default true)
  - `deliveryco.store-webhook.base-url`: Store base URL (default `http://localhost:8080`)
  - `deliveryco.store-webhook.path`: Callback path (default `/api/delivery/status-callback`)
  - `deliveryco.store-webhook.secret-header`: Header name for shared secret (default `X-DeliveryCo-Secret`)
  - `deliveryco.store-webhook.secret-value`: Secret value (must match Store)

- Payload example (POST `${base-url}${path}`)
```json
{
  "eventId": "81fe8112-70fa-48d5-9470-697150cdd283",
  "externalOrderId": "ORD-1234",
  "status": "IN_TRANSIT",
  "reason": "Package out for delivery",
  "occurredAt": "2025-10-17T10:33:32.790Z"
}
```

- Behavior
  - Fire-and-forget (non-blocking WebClient); failures are logged and do not affect internal processing or Kafka publishing.
  - Include the shared-secret header if configured.

### Status Lifecycle

| Stage        | Description                                      |
|--------------|--------------------------------------------------|
| `RECEIVED`   | Order persisted and acknowledged                 |
| `PICKED_UP`  | Goods collected from warehouse                   |
| `IN_TRANSIT` | Out for delivery                                 |
| `DELIVERED`  | Delivery confirmed at destination                |
| `LOST`       | Loss detected (probability driven by `lossRate`) |
| `CANCELLED`  | Cancelled by store before completion             |

### Database Schema

Flyway migrations create tables for orders, items, status history, durable jobs, outbox, incidents, and worker heartbeats. See `src/main/resources/db/migration` for DDL.

### Local Development Workflow

1. `docker compose up -d` – start Postgres, Kafka, ZooKeeper, pgAdmin.
2. `./gradlew bootRun` – launch DeliveryCo.
3. Send deliveries via REST or Kafka; monitor status topic and pgAdmin tables.
4. `./gradlew clean test` – run Testcontainers-backed integration tests.

### Observability & Operations

- Actuator endpoints: `GET /actuator/health`, `/actuator/metrics`, `/actuator/prometheus`.
- Heartbeats stored in `delivery_worker_heartbeat` to monitor worker liveness.
- Incidents stored in `delivery_incident` for auditing lost packages or system issues.

### Extensibility Notes

- Add authentication by enabling Spring Security (e.g., JWT) on REST and Kafka channels.
- Customize loss probabilities or job timing via `deliveryco.scheduler.*` properties.
- Extend the outbox publisher to include Schema Registry or Avro payloads if required by downstream consumers.

## Email Service + UI

We include a companion `email_service` app that consumes `delivery.status` events and renders a minimal inbox UI (SSE) for demos.

How it’s implemented
- Producer settings: DeliveryCo disables JSON type headers to simplify cross-service consumption.
- Payload enrichment: every status event includes `payload.contactEmail` so the EmailService knows where to send.
- Idempotency: EmailService stores one email per `(toAddress, externalOrderId, messageType)` using a unique index and code-level pre-check. Email addresses are normalized to lower-case trimmed.
- UI: `http://localhost:8081` serves a static inbox that live-updates via SSE and de-dupes by email row ID.
- Schema isolation: DeliveryCo migrates in schema `public`; EmailService migrates in schema `email` and uses `flyway_schema_history_email`.

Run both services locally
1) Start infra (Kafka, ZooKeeper, Postgres, pgAdmin)
- `cd delivery-co`
- `docker compose up -d`

2) Start DeliveryCo
- `./gradlew bootRun`

3) Start EmailService
- `cd ../email_service`
- `gradle bootRun`

## End-to-End Demo (Webhook + Kafka)

This demo shows DeliveryCo updating Store via REST (webhook) while EmailService consumes Kafka for customer emails.

Prerequisites
- Docker Desktop running.
- Compose stack up (Postgres, Kafka, ZooKeeper):
  - `docker compose -f delivery-co/docker-compose.yml up -d`
- Secrets aligned (shared secret for webhook):
  - Store: `app.delivery.secret: change-me`
  - DeliveryCo: `deliveryco.store-webhook.secret-value: change-me`

Ports (recommended)
- Store on `8080`
- DeliveryCo on `8082` (to avoid port clash with Store)
- EmailService on `8081`

Start apps
- Terminal A: `cd store && ./gradlew bootRun`
- Terminal B: `cd delivery-co && ./gradlew bootRun --args='--server.port=8082'`
- (Optional) Terminal C: `cd email_service && gradle bootRun`

Seed Store with data (copy/paste commands; adjust values if needed)
1) Create a customer (save the `id` from the response)
```
curl -sS -X POST http://localhost:8080/api/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "email":"customer+demo@gmail.com",
    "password":"123456",
    "role":"CUSTOMER",
    "firstName":"Jane",
    "lastName":"Doe",
    "phone":"123-456-7890",
    "address":"123 Test St"
  }'
```
2) Create a product (save the `productCode`)
```
curl -sS -X POST http://localhost:8080/api/products \
  -H 'Content-Type: application/json' \
  -d '{ "productName":"Widget Demo", "description":"Demo item", "price":12.50 }'
```
3) Place an order (save the `orderNumber` — this becomes `externalOrderId`)
```
curl -sS -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -d '{
    "customerId":"<PASTE_CUSTOMER_ID>",
    "deliveryAddress":"123 Test St",
    "shipping":5.00,
    "orderItems":[{"productCode":"<PASTE_PRODUCT_CODE>","quantity":2}]
  }'
```

Kick off DeliveryCo for that order
```
curl -sS -X POST http://localhost:8082/api/deliveries \
  -H 'Content-Type: application/json' \
  -d '{
    "externalOrderId":"<PASTE_STORE_ORDER_NUMBER>",
    "customerId":"C-DEMO",
    "pickupLocations": { "WH-1":"1 Warehouse Way" },
    "dropoffAddress":"22 Customer St",
    "contactEmail":"demo@customer.local",
    "lossRate":0.05,
    "items": { "WH-1": {"SKU-1":1} }
  }'
```

Verify Store status updates (DeliveryCo → Store webhook)
- Poll the order (allow ~10–20s between polls):
```
curl -sS http://localhost:8080/api/orders/<PASTE_STORE_ORDER_NUMBER>
```
- Expected: status progresses from `PENDING` → `PAID` → `SHIPPED`.

Optional: Customer email UI (Kafka path)
- Open `http://localhost:8081`, enter `demo@customer.local`, click “Load”.
- Expect exactly one email per status (deduplicated).

Troubleshooting
- 403 when POSTing `/api/deliveries`: You hit Store (8080). DeliveryCo is on 8082.
- DeliveryCo DB connection refused: ensure Postgres is up on port 15432 (`docker compose -f delivery-co/docker-compose.yml ps`).
- Store stays `PENDING`: either DeliveryCo didn’t progress yet (wait or reduce delay), or webhook secret mismatch. Test receiver directly:
```
curl -X POST http://localhost:8080/api/delivery/status-callback \
  -H 'Content-Type: application/json' \
  -H 'X-DeliveryCo-Secret: change-me' \
  -d '{"eventId":"00000000-0000-0000-0000-000000000000","externalOrderId":"<ORDER>","status":"IN_TRANSIT","reason":"manual","occurredAt":"2025-11-01T06:00:00Z"}'
```
- Secrets must match: `deliveryco.store-webhook.secret-value` (DeliveryCo) vs `app.delivery.secret` (Store).

4) Open the inbox UI
- Visit `http://localhost:8081`
- Enter `demo@customer.local` and click “Load”

5) Trigger a delivery
```bash
curl -X POST http://localhost:8080/api/deliveries \
  -H 'Content-Type: application/json' \
  -d '{
    "externalOrderId":"ORD-DEMO-UI",
    "customerId":"C-DEMO",
    "pickupWarehouseId":"WH-1",
    "pickupAddress":"1 Warehouse Way",
    "dropoffAddress":"22 Customer St",
    "contactEmail":"demo@customer.local",
    "lossRate":0.05,
    "items":[{"sku":"SKU-1","description":"Widget","quantity":1}]
  }'
```

Expected result
- One email per status transition (RECEIVED, PICKED_UP, IN_TRANSIT, DELIVERED or LOST) appears live; reloading still shows one per status.

### Troubleshooting
- DeliveryCo Flyway conflicts after manual DB changes
  - Clean reset: terminate sessions and recreate DB `deliveryco`:
    - `docker compose -f delivery-co/docker-compose.yml exec -T postgres psql -U deliveryco -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='deliveryco';"`
    - `docker compose -f delivery-co/docker-compose.yml exec -T postgres psql -U deliveryco -d postgres -c "DROP DATABASE IF EXISTS deliveryco; CREATE DATABASE deliveryco;"`
  - Or drop DeliveryCo tables:
    - `docker compose -f delivery-co/docker-compose.yml exec -T postgres psql -U deliveryco -d deliveryco -c "DROP TABLE IF EXISTS delivery_item, delivery_status_event, delivery_job, delivery_outbox, delivery_incident, delivery_worker_heartbeat, delivery_order CASCADE;"`
- EmailService 404 on UI or APIs
  - Ensure EmailService runs on 8081 and static files exist under `email_service/src/main/resources/static`.
- EmailService Flyway checksum mismatch
  - Dev-only repair bean updates checksums on startup. For strict validation, remove it and drop schema `email`:
    - `docker compose -f delivery-co/docker-compose.yml exec -T postgres psql -U deliveryco -d deliveryco -c "DROP SCHEMA IF EXISTS email CASCADE; CREATE SCHEMA email;"`
- No emails arriving
  - Check Kafka messages: `docker compose -f delivery-co/docker-compose.yml exec kafka kafka-console-consumer --topic delivery.status --bootstrap-server localhost:19092 --from-beginning`
  - Ensure the UI email matches the `contactEmail` you send.

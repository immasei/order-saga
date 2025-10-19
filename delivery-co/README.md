## DeliveryCo Service

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

## Integration Guide

DeliveryCo exposes both synchronous and asynchronous entry points so other services can request deliveries and track progress.

### REST API

**Endpoint:** `POST /api/deliveries`

**Request body (`application/json`):**

```json
{
  "externalOrderId": "ORD-1234",
  "customerId": "C-1",
  "pickupWarehouseId": "WH-9",
  "pickupAddress": "1 Warehouse Way",
  "dropoffAddress": "77 Client Road",
  "contactEmail": "customer@example.com",
  "lossRate": 0.05,
  "items": [
    { "sku": "SKU-1", "description": "Widget", "quantity": 2 }
  ]
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
  "pickupWarehouseId": "WH-9",
  "pickupAddress": "1 Warehouse Way",
  "dropoffAddress": "77 Client Road",
  "contactEmail": "customer@example.com",
  "lossRate": 0.05,
  "items": [
    { "sku": "SKU-1", "description": "Widget", "quantity": 2 }
  ]
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

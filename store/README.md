# STORE API README

## latest update (10:00 am thu 30)
### Docker: kafka and postgre
 - turn on 
    ```
    docker compose up -d 
    ```

 - turn off
    ```
    docker compose stop
    ```
    
 - remove
   ```
   docker compose down
   ```

- visit pgadmin http://localhost:5050
- visit kafkaui http://localhost:6060

### saga & kafka & outbox
- see `kafka/` folder
- saga orchestrator only listen to `event/` and create outbox records for `command/`
- other handlers only listen to `command/` and create outbox records for `event/`
- `OutboxPublisher` will be scheduled to kafka.send outbox records.
- try create order you should see some warning logs.

### DeliveryCo → Store Webhook (status updates)

Store exposes a REST endpoint to accept delivery status callbacks from DeliveryCo.

- Endpoint
  - `POST /api/delivery/status-callback`
  - Header: `X-DeliveryCo-Secret: <shared-secret>` (must match `app.delivery.secret`)
- Body (JSON)
```
{
  "eventId": "uuid",
  "externalOrderId": "ORD-1234",
  "status": "RECEIVED|PICKED_UP|IN_TRANSIT|DELIVERED|LOST|CANCELLED",
  "reason": "...",
  "occurredAt": "2025-10-17T10:33:32.790Z"
}
```
- Mapping to Store status
  - RECEIVED → RESERVED
  - PICKED_UP, IN_TRANSIT → SHIPPED (adjust if you add an IN_DELIVERY state)
  - DELIVERED → SHIPPED (or COMPLETED if you add it)
  - LOST, CANCELLED → CANCELLED

Configure the shared secret
- In `store/src/main/resources/application.yml`: `app.delivery.secret: change-me`
- In DeliveryCo: `deliveryco.store-webhook.secret-value: change-me`

### End-to-End Demo (copy/paste)

Prereqs
- Docker Desktop running. Compose up (DB/Kafka):
  - `docker compose -f delivery-co/docker-compose.yml up -d`
- Secrets aligned in Store + DeliveryCo as above.
- Ports: Store on 8080; DeliveryCo on 8082; EmailService on 8081 (optional).

Run apps
- Terminal A: `cd store && ./gradlew bootRun`
- Terminal B: `cd delivery-co && ./gradlew bootRun --args='--server.port=8082'`

Seed Store
1) Create a customer (copy `id` from the response):
```
curl -sS -X POST http://localhost:8080/api/customers -H 'Content-Type: application/json' -d '{
  "email":"customer+demo@gmail.com","password":"123456","role":"CUSTOMER",
  "firstName":"Jane","lastName":"Doe","phone":"123-456-7890","address":"123 Test St"
}'
```
2) Create a product (copy `productCode`):
```
curl -sS -X POST http://localhost:8080/api/products -H 'Content-Type: application/json' -d '{
  "productName":"Widget Demo","description":"Demo item","price":12.50
}'
```
3) Place an order (copy `orderNumber` — becomes externalOrderId):
```
curl -sS -X POST http://localhost:8080/api/orders -H 'Content-Type: application/json' -d '{
  "customerId":"<PASTE_CUSTOMER_ID>",
  "deliveryAddress":"123 Test St",
  "shipping":5.00,
  "orderItems":[{"productCode":"<PASTE_PRODUCT_CODE>","quantity":2}]
}'
```

Kick off DeliveryCo for that order (DeliveryCo on 8082)
```
curl -sS -X POST http://localhost:8082/api/deliveries -H 'Content-Type: application/json' -d '{
  "externalOrderId":"<PASTE_STORE_ORDER_NUMBER>",
  "customerId":"C-DEMO",
  "pickupLocations": { "WH-1":"1 Warehouse Way" },
  "dropoffAddress":"22 Customer St",
  "contactEmail":"demo@customer.local",
  "lossRate":0.05,
  "items": { "WH-1": {"SKU-1":1} }
}'
```

Verify status change in Store
```
curl -sS http://localhost:8080/api/orders/<PASTE_STORE_ORDER_NUMBER>
```
Expected progression over ~10–20s: `PENDING` → `PAID` → `SHIPPED`.

Optional
- EmailService: `cd email_service && gradle bootRun` then open `http://localhost:8081` and enter `demo@customer.local` to see one email per status.

Troubleshooting tips
- 403 when POSTing `/api/deliveries`: you’re hitting Store (8080). DeliveryCo is on 8082.
- Webhook rejected (401): secrets don’t match; set `app.delivery.secret` in Store and `deliveryco.store-webhook.secret-value` in DeliveryCo to the same value.
- No status change: test the webhook directly:
```
curl -X POST http://localhost:8080/api/delivery/status-callback \
  -H 'Content-Type: application/json' -H 'X-DeliveryCo-Secret: change-me' \
  -d '{"eventId":"00000000-0000-0000-0000-000000000000","externalOrderId":"<ORDER>","status":"IN_TRANSIT","reason":"manual","occurredAt":"2025-11-01T06:00:00Z"}'
```
### DeliveryCo → Store Webhook (status updates)

Store exposes a REST endpoint to accept delivery status callbacks from DeliveryCo.

- Endpoint
  - `POST /api/delivery/status-callback`
  - Header: `X-DeliveryCo-Secret: <shared-secret>` (must match `app.delivery.secret`)
- Body (JSON)
```
{
  "eventId": "uuid",
  "externalOrderId": "ORD-1234",
  "status": "RECEIVED|PICKED_UP|IN_TRANSIT|DELIVERED|LOST|CANCELLED",
  "reason": "...",
  "occurredAt": "2025-10-17T10:33:32.790Z"
}
```
- Mapping to Store status
  - RECEIVED → RESERVED
  - PICKED_UP, IN_TRANSIT → SHIPPED (or adjust as needed)
  - DELIVERED → SHIPPED (or COMPLETED if you add it)
  - LOST, CANCELLED → CANCELLED

Configure shared secret (must match DeliveryCo):
- `app.delivery.secret: change-me` in `store/src/main/resources/application.yml`
- In DeliveryCo: `deliveryco.store-webhook.secret-value: change-me`
   
### Note

>Note: if you change/add new @Entity, please drop the store db, create a new on in pgadmin, then
>  - Option 1 (current): update the schema(s) in resources/db/migration/V1_init.sql. its because i set ddl-auto to validate, which means jpa wont create/alter the schema, it just validate.
>
>  - Option 2: set to ddl-auto to update, may have some error messages about constraint already created, but it runs fine

---

## Overview

This project implements a **store management system** using Spring Boot. It includes:

Polymorphism and inheritance are used to generalize behavior:

- **User → Customer / Admin**
  - `User` is the parent class with fields like `email`, `password`, `role`
  - `Customer` and `Admin` extend `User` and add specific fields (e.g., `phone`, `address` for Customer)
- **Warehouse → warehouse_stock_{warehouseCode}**
  - `Warehouse` holds generic warehouse information
  - `warehouse_stock_{warehouseCode}` extend the concept to track **stock per warehouse**, enabling polymorphic handling of warehouse inventory

---

## API Endpoints

### 1. Auth Management

```
[Client]                         [Backend]
   │   POST api/auth/login
   ├──────────────────────────────►   create tokens
   │   ◄────────────────────────────  accessToken (body)
   │                                  refreshToken (HttpOnly cookie)
   │
   │  GET /api/<smt> (Authorization: Bearer <accessToken>)
   ├──────────────────────────────►   validate access token
   │
   │  accessToken expires
   │
   │  POST api/auth/refresh (cookie auto-sent)
   ├──────────────────────────────►   validate refresh, issue new access
   │   ◄────────────────────────────  new accessToken (body)

``` 

#### Signup (create Customer only)

- **POST**
    ```
    http://localhost:8080/api/auth/signup
    ```

    ```json
    {
      "email": "a@example.com",
      "password": "123456",
      "firstName": "Mickey",
      "lastName": "Mouse",
      "role": "CUSTOMER",
      "address": "123 Main St, Springfield, IL",
      "phone": "123-456-7890"
    }
    ```
- **Body**={`SignUpDTO`, `CreateCustomerDTO`}
- **Note**: Please save the token and put it in Auth - Bearer Token

#### Login

- **POST**
    ```
    http://localhost:8080/api/auth/login
    ```

    ```json
    {
      "email": "admin@example.com",
      "password": "supersaiya"
    }
    ```
- **Body**={`LoginDTO`}
- **Note**:
  - **This is the default admin account** (editable in `application.properties`)
  - Please save the token and put it in Auth - Bearer Token


### 2. User Management

#### Create Admin (admin only)

- **POST**
    ```
    http://localhost:8080/api/admins
    ```

    ```json
    {
        "email": "admin@gmail.com",
        "password": "123456",
        "role": "ADMIN",
        "firstName": "Pika",
        "lastName": "Chu"
    }
    ```
- **Body**={`SignUpDTO`, `CreateAdminDTO`}

#### Create Customer

- **POST**
    ```
    http://localhost:8080/api/customers
    ```

    ```json
    {
      "email": "customer@gmail.com",
      "password": "123456",
      "role": "CUSTOMER",
      "firstName": "firstName",
      "lastName": "lastName",
      "phone": "123-456-7890",
      "address": "123 Main St, Springfield, IL"
    }
    ```
- **Body**={`SignUpDTO`, `CreateCustomerDTO`}

### 3. Product Management

#### Create 1 Product

- **POST**
    ```
    http://localhost:8080/api/products
    ```

    ```json
    {
      "productName": "Macbook Pro",
      "description": "very pro",
      "price": 312.54
    }
    ```

- **Body**={`CreateProductDTO`}

#### Create N Products

- **POST**
    ```
    http://localhost:8080/api/products/batch
    ```
    ```json
    [
        {
          "productName": "Ipad Pro",
          "description": "11 inch",
          "price": 124.23
        },
        {
          "productName": "Iphone X",
          "description": "very old",
          "price": 454.76
        },
        {
          "productName": "Apple Watch",
          "description": "44mm",
          "price": 193.20
        }
    ]
    ```
- **Body**={List<`CreateProductDTO`>}
- **Note**: It's atomic: All or nothing created.

#### Get all products
- **GET**
    ```
    http://localhost:8080/api/products
    ```

#### Get warehouse by productCode
- **GET**
    ```
    http://localhost:8080/api/products/{productCode}
    ```
    ```
    http://localhost:8080/api/products/PRD-01K8PBBTMEE44JANFWY1V719F0
    ```
- **Note**:
  - `productCode` is case sensitive

### 4. Warehouse Management

- **Note**:
  - When you create warehouse(s), the `WarehouseStockManager` will dynamically create a `warehouse_stock_{warehouseCode}` table in PgAdmin
    - Where: open pgadmin > `warehouse_stock` > partitions
  - Base Entity is `model/Stock` (Table name=`warehouse_stock`).  This table is partitioned by `warehouse_id`, which automatically creates separate partitions (aka table) for different `warehouse_id` (see `db/migration/v1_init.sql`)
  - We can still use `StockRepository` (based on the base entity) to find by (`productId`, `warehouseId`).
  - When inserting stock by (`productId`, `warehouseId`), the data is automatically inserted into the corresponding partition table.

#### Create 1 Warehouse

- **POST**
    ```
    http://localhost:8080/api/warehouses
    ```

    ```json
    {
      "warehouseCode": "SYD-01",
      "warehouseName": "Warehouse S01",
      "location": "Sydney, Australia"
    }
    ```
- **Body**={`CreateWarehouseDTO`}
- **Note**: in this example, open pgadmin > `warehouse_stock` > partitions > you can see new table `warehouse_stock_syd_01`

#### Create N Warehouses

- **POST**
    ```
    http://localhost:8080/api/warehouses/batch
    ```

    ```json
    [
        {
           "warehouseCode": "TAS-01",
           "warehouseName": "Warehouse T01",
           "location": "Tasmania, Australia"
        },
        {
          "warehouseCode": "MEL-01",
          "warehouseName": "Warehouse M01",
          "location": "Melbourne, Australia"
        },
        {
          "warehouseCode": "SYD-02",
          "warehouseName": "Warehouse S02",
          "location": "Sydney, Australia"
        }
    ]
    ```
- **Body**={List<`CreateWarehouseDTO`>}
- **Note**: It's atomic: All or nothing updated/created.

#### Get all warehouses
- **GET**
    ```
    http://localhost:8080/api/warehouses
    ```

#### Get warehouse by warehouseCode
- **GET**
    ```
    http://localhost:8080/api/warehouses/{warehouseCode}
    ```
    ```
    http://localhost:8080/api/warehouses/SYD-02
    ```
- **Note**: `warehouseCode` is case sensitive

### 5. Warehouse Stock Management

#### Add/Update Stock of 1 product to 1 warehouse

- **POST**
    ```
    http://localhost:8080/api/stocks
    ```

    ```json
    {
      "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
      "warehouseCode": "SYD-02",
      "quantity": 23
    }
    ```
- **Body**={`AssignStockDTO`}
- **Note**:
  - Try run this 1 more time, it will double up the stocks.
  - `productCode` & `warehouseCode` are case sensitve

#### Add/Update Stock of N product to N warehouse

- **POST**
    ```
    http://localhost:8080/api/stocks/batch
    ```

    ```json
    [
        {
          "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
          "warehouseCode": "SYD-02",
          "quantity": 26
        },
        {
          "productCode": "PRD-01K8PBBTMEE44JANFWY1V719F1",
          "warehouseCode": "SYD-02",
          "quantity": 25
        },
        {
          "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
          "warehouseCode": "MEL-01",
          "quantity": 22
        }
    ]
    ```
- **Body**={List<`AssignStockDTO`>}
- **Note**:
  - It's atomic: All or nothing updated/created.
  - `productCode` & `warehouseCode` are case sensitve

#### Get stocks of 1 product in all warehouses
- **GET**
    ```
    http://localhost:8080/api/stocks/{productcode}
    ```
    ```
    http://localhost:8080/api/stocks/PRD-01K8PBBTMEE44JANFWY1V719F0
    ```

- **Note**:
  - `productCode` is case sensitve

#### Get stocks of 1 warehouse
- **GET**
    ```
    http://localhost:8080/api/warehouses/{warehouseCode}/stocks
    ```
    ```
    http://localhost:8080/api/warehouses/SYD-02/stocks
    ```
- **Note**:
  - `warehouseCode` is case sensitive

### 6. Order Management
#### Create Order

- **POST**
    ```
    http://localhost:8080/api/orders
    ```
    ```json
    {
      "customerId": "2439f2f3-01d6-46bf-9933-f8e5b48778f4",
      "deliveryAddress": "Rockdale 2216",
      "shipping": 45,
      "orderItems": [
        {
          "productCode": "PRD-01K8QE83388P0HSE45E4378SXT",
          "quantity": 2
        },
        {
          "productCode": "PRD-01K8QE83388P0HSE45E4378SXV",
          "quantity": 3
        }
      ]
    }
    ```
- **Body**={`CreateOrderDTO`, List<`CreateOrderItemDTO`>}
- **Note**:
  - `deliveryAddress` is optional, if not provided use Customer.address (saved when create account)
  - `productCode` is case sensitive

#### Get order by order number
- **GET**
    ```
    http://localhost:8080/api/orders/{orderNumber}
    ```
    ```
    http://localhost:8080/api/orders/ORD-01K8QGFHNXF1H8T345WFJJZZ6X
    ```
- **Note**:
  - `orderNumber` is case sensitive

#### Get all orders
- **GET**
    ```
    http://localhost:8080/api/orders
    ```

#### Get all orders of 1 customer
- **GET**
    ```
    http://localhost:8080/api/customers/{customerId}/orders
    ```
    ```
    http://localhost:8080/api/customers/2439f2f3-01d6-46bf-9933-f8e5b48778f4/orders
    ```

### Polymorphism & Inheritance Notes
- User → Customer / Admin
  -  Treat all users generically when needed (e.g., login, listing users)
  -  Admin-specific and Customer-specific fields are only available when casting to the correct type
- Warehouse → Warehouse1Stock / Warehouse2Stock
  - Warehouse contains common information like name and location
  - Warehouse1Stock & Warehouse2Stock contain stock information per warehouse
  - Can handle multiple warehouse types polymorphically for stock management
- OrderItems → ProductPurchaseHistory Service
  - ProductPurchaseHistoryService updates purchase history from OrderItems
  - Demonstrates composition and aggregation with inheritance


    classDiagram
    User <|-- Admin
    User <|-- Customer

    Warehouse <|-- Warehouse1Stock
    Warehouse <|-- Warehouse2Stock

    OrderItems --> ProductPurchaseHistoryService


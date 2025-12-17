# Store as Main Service

## Frontend & Backend
```
http://localhost:8080
```

## How to Run
### Docker: Setup kafka and postgre

- Please run the `docker-compose.yml` in `store/` only
    ```
    cd store
    ```

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
     "username": "customer",
     "email": "comp5348@example.com",
     "password": "COMP5348",
     "firstName": "Demo",
     "lastName": "Account",
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
      "username": "customer",
      "password": "COMP5348"
    }
    ```
    ```json
    {
      "username": "admin",
      "password": "supersaiya"
    }
    ```
- **Body**={`LoginDTO`}
- **Note**:
  - **Default admin account** is editable in `application.properties`
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
      "username": "customer",
      "email": "comp5348@example.com",
      "password": "COMP5348",
      "firstName": "Demo",
      "lastName": "Account",
      "role": "CUSTOMER",
      "address": "123 Main St, Springfield, IL",
      "phone": "123-456-7890"
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
        },
        {
          "productName": "Notebook",
          "description": "Freebies",
          "price": 0
        },
        {
          "productName": "Golden Macbook",
          "description": "Super expensive",
          "price": 10000000
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

#### Get product by productCode
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
      "paymentAccountRef": "BAC-01K8X2KFVS6HWT8VNMZMFY67EK",
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
  - please create a bank account using Bank's frontend `localhost:8082` with sufficient funds (use `deposit`) and copy the `accountRef` here (`BAC-<smt>`)

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

### DeliveryCo → Store Webhook (status updates on delivery)

Store exposes a REST endpoint to accept delivery status callbacks from DeliveryCo.

- **POST**
    ```
    http://localhost:8080/api/delivery/status-callback
    ```

- **Note**
  - Header: `X-DeliveryCo-Secret: <shared-secret>` (must match `app.delivery.secret`)
  - Body (JSON)
    ```json
    {
        "eventId": "uuid",
        "externalOrderId": "ORD-1234",
        "status": "RECEIVED|PICKED_UP|IN_TRANSIT|DELIVERED|LOST|CANCELLED",
        "reason": "...",
        "occurredAt": "2025-10-17T10:33:32.790Z"
    }
    ```

    - `RECEIVED` > `OrderStatus.AWAIT_CARRIER_PICKUP`
    - `PICKED_UP`, `IN_TRANSIT` > `OrderStatus.IN_TRANSIT`
    - `DELIVERED` > `DELIVERED`
    - `LOST`, `CANCELLED` > `LOST_IN_DELIVERY`

  - Configure the shared secret
    - In `store/src/main/resources/application.yml`: `app.delivery.secret: change-me`
    - In DeliveryCo: `deliveryco.store-webhook.secret-value: change-me`

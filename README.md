# TUT12-Group02

[How to Run](#how-to-run)
   - [1.1 Docker: Setup Kafka and PostgreSQL](#11-docker-setup-kafka-and-postgresql)
   - [1.2 Run 3 applications in Intellj](#12-run-3-applications-in-intellj)
   - [1.3 Setup Data](#13-setup-data)

[Simulating Order Flow](#simulating-order-flow)
   - [0) Happy Path](#0-happy-path)
   - [1) Create Shipment Failed](#1-create-shipment-failed)
   - [2) Create Payment Failed](#2-create-payment-failed)
   - [3) Cancel During Happy Path](#3-cancel-during-happy-path)
   - [4) Cancel When Already Cancelling/Cancelled](#4-cancel-when-already-cancellingcancelled)
   - [5) Inventory Out of Stock](#5-inventory-out-of-stock)
   - [6) Insufficient Balance](#6-insufficient-balance)
   - [7) Zero Amount Payment (Free Product)](#7-zero-amount-payment-free-product)

## How to Run
### 1.1 Docker: Setup kafka and postgresql

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

- Visit pgadmin http://localhost:5050

  - **Step 1**: login with

    ```
    admin@comp5348.com
    ```
    ```
    changeme
    ```

  - **Step 2**: Register a server

    - General > Server name: 

      ```
      tut12g02
      ```
  
    - Connection > Host name:

      ```
      postgres
      ```

    - Connection > Username:

      ```
      comp5348
      ```

    - Connection > Password:

      ```
      changeme
      ```
  - **Step 3**: Create 3 databases with the exact name below

      ```
      store
      ```
      ```
      bank
      ```
      ```
      deliveryco
      ```


### 1.2 Run 3 applications in Intellj

- **DeliveryCo** (backend only)

    ```
    http://localhost:8081
    ```

- **Bank** (backend + frontend)

    ```
    http://localhost:8082
    ```

- **EmailService** (backend + frontend)

    ```
    http://localhost:8083
    ```

- We need to put store's bank account in `Store`'s `application.properties` (next step) before we run it.

### 1.3 Setup data

- **Step 1**: Open Bank's frontend on 8082, create 2 bank accounts

    - First create customer, copy `customerRef` "CLI-<something>", then create Account using `customerRef`, copy `accountRef` "BAC-<something>".

    - **With Customer's bank account**: Deposit in 10000. Save the `accountRef` somewhere so when a customer place an order, they need to input there bank account

    - **With Store's bank account**: Copy the `accountRef`. Go to `store/application.properties`, update store's bank account under `store.bank.account.ref`

- **Step 2**: Open Postman to create products/ warehouses and stocks

  - Run `Store` in Intellij. This is the endpoint for both frontend and backend.

    ```
    http://localhost:8080
    ```

  - **Sign in** as an Admin: `POST`
    ```
    http://localhost:8080/api/auth/login
    ```
    ```json
      {
        "username": "admin",
        "password": "supersaiya"
      }
    ```
    - copy the accessToken, go to Auth tab, put them under Bearer Token (this token last for 20 minutes)
    
  - **Create products**: `POST`

    ```
    http://localhost:8080/api/products/batch
    ```
    ```json
    [
        {
          "productName": "Ipad Pro",
          "description": "11 inch",
          "price": 10
        },
        {
          "productName": "Iphone X",
          "description": "very old",
          "price": 20
        },
        {
          "productName": "Apple Watch",
          "description": "44mm",
          "price": 30
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

    - copy the `product code`(s) in the response, you need them when we assign stocks

    - or just called `GET`

      ```
      http://localhost:8080/api/products

      ```

  - **Create warehouses**: `POST`

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

  - **Assign stocks to warehouses**: `POST`

    ```
    http://localhost:8080/api/stocks/batch
    ```

    ```json
    [
        {
          "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
          "warehouseCode": "SYD-02",
          "quantity": 50
        },
        {
          "productCode": "PRD-01K8PBBTMEE44JANFWY1V719F1",
          "warehouseCode": "SYD-02",
          "quantity": 30
        },
        {
          "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
          "warehouseCode": "MEL-01",
          "quantity": 70
        },
        {
          "productCode": "PRD-01K8P69SD5J4T5PK8PVXF0Y07E",
          "warehouseCode": "TAS-01",
          "quantity": 20
        },
        {
          "productCode": "PRD-01K8PBBTMEE44JANFWY1V719F1",
          "warehouseCode": "MEL-01",
          "quantity": 40
        }
    ]
    ```

    - Only need to change the `product code`


- **Step 3**: Open Postman, create a Customer 

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

- **Step 4**: Open Store's frontend on `8080` to start ordering.

  - **Sign in** as an CUSTOMER using the account that we have created in step 2

    ```
    customer
    ```
    ```
    COMP5348
    ```


- After order, you can see the Order shown on the page. Refresh continuously to see updated status on frontend, which is every fast

  - I suggest you observe the SAGA flow through Store's logs.


## Simulating Order flow

- Open

    ```
    store/application.properties
    ```

    ```
    delivery.loss.rate=0.05
    demo.cancelled.aft-order-placed-be4-reserved=false
    demo.cancelled.aft-reserved-be4-paid=false
    demo.cancelled.aft-paid-be4-shipped=false
    demo.cancelled.aft-shipped=false
    demo.refund.failed=false
    demo.cancelled.aft-payment-failed=false
    demo.cancelled.aft-shipment-failed=false
    demo.cancelled.aft-delivery-lost=false
    ```
    
    - it should be all false.

#### 0) Happy path

    Ensure all `demo.*` flags are `false`, and keep lost rate very low.

- Runs `store`
- Runs `bank`
- Runs `delivery-co`
- Runs `email-service`
- Submit an order (sufficient funds, not out of stock)
- After `store`'s log email, please wait a bit for `delivery co` to send delivery updates.
- See log output

#### 1) Create shipment failed

- Shut down the `delivery co`

- Expect a refund, a release, order cancellation and an email

#### 2) Create payment failed

- Shut down the `bank`

- Expect a release, order cancellation and an email

#### 3) Cancel during Happy Path
Set exactly one:
- `demo.cancelled.aft-order-placed-be4-reserved=true`  
  _Cancel immediately after order is placed (expect no release/ cancellable)._

- `demo.cancelled.aft-reserved-be4-paid=true`  
  _Cancel after inventory is reserved but before payment (expect no payment charged/ release/ cancellable)._

- `demo.cancelled.aft-paid-be4-shipped=true`  
  _Cancel after payment, before shipment (expect refund / cancellable / not shipped)._

- `demo.cancelled.aft-shipped=true`  
  _Cancel after shipment started (expect non cancellable)._

Optional: to simulate refund failure when the order was paid, set:

- `demo.refund.failed=true`  
  _Payment succeed before eligible for a refund but refund failed (expect cancellable, cancelled_required_manual_refund). Can combine this event with ShipmentFailed (just shut down `delivery co`) or `aft.paid.be4.shipped`_

#### 4) Cancel when Already Cancelling/Cancelled
Pick one:

- `demo.cancelled.aft-payment-failed=true`  
  _When payment error happens ie bank down, insufficient funds in bank account, order will be cancelled due to payment failed. But customer request cancellation at the same time (expect a release, 2 emails 1: - already_cancelled, 2: cancel due to payment failed)._

- `demo.cancelled.aft-shipment-failed=true` 
  _When shipment error happens ie delivery co down, order will be cancelled due to shipment failed. But customer request cancellation at the same time (expect a refund/ release/ 2 emails 1: - already_cancelled, 2: cancel due to shipment failed)._

- `demo.cancelled.aft-delivery-lost=true` **and** `delivery.loss.rate=1.0`  
  _(force "lost_in_delivery" in application.properties `delivery.loss.rate=1`. Upon lost_in_delivery, order will be cancelled. But customer request cancellation at the same time (expect a full refund, no release, 2 emails 1: - already_cancelled, 2: cancel due to lost in delivery)._
  (forces “lost in transit” → full refund scenario)

#### 5) Inventory Out of Stock
- Keep demo flags `false`.
- When placing an order, set **quantity to 1 million to trigger `OUT_OF_STOCK`.

#### 6) Insufficient Balance
- Keep demo flags `false`.
- Create a product with a **very high price** and attempt payment to trigger **insufficient funds** error from Bank.

#### 7) Zero Amount Payment (Free Product)
- Create product with **price = 0**.
- When creating the order, set **shipping = 0**.
- Expected: system should skip the bank call for payment (zero amount) and proceed to next stage

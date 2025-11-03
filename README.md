# DEMO

## Base Urls

- **Store** (backend + frontend)

    ```
    http://localhost:8080
    ```

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

## Before Demo

```
All routes except /api/auth/** & /api/delivery/** are authenticated.
The access token is currently set to 20 mins.
If you use frontend, the access token will automatically refreshed.
```

- create 2 bank accounts using Bank's frontend

    - **Customer's bank account**: should have some money. Copy the `accountRef`, input when place order.

    - **Store's bank account**: 0 money is ok. Copy the `accountRef`. Go to `store/application.properties`, update store's bank account. `store.bank.account.ref`

- login by default admin account for postman

    ```
    http://localhost:8080/api/auth/login
    ```
    ```json
    {
        "username": "admin",
        "password": "supersaiya"
    }
    ```

- create & login by customer account for store's frontend
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

## Demo: Order flow

<maybe a flow chart here>

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

#### 0) Happy path

    Ensure all `demo.*` flags are `false`, and keep lost rate very low.

- Runs `store`

- Runs `bank`

- Runs `delivery-co`

- Runs `email-service`

- Submit an order (sufficient funds, not out of stock)

- After store's email, please wait a bit for delivery co to send delivery updates.

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
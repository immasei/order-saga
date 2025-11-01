
# Bank as an External service
## Base url

- Backend & Frontend

    ```
    http://localhost:8082/
    ```

## Routes summary
```
POST   /api/customers
GET    /api/customers
GET    /api/customers/{customerRef}
GET    /api/customers/{customerRef}/accounts
GET    /api/customers/{customerId}/accounts/{accountRef}

POST   /api/accounts
GET    /api/accounts
GET    /api/{accountRef}

POST   /api/transfer
POST   /api/deposit
POST   /api/withdraw
POST   /api/refund
```

### Create 2 Customers

- **POST**
    ```
    http://localhost:8082/api/customers
    ```
    ```json
    {
        "firstName": "Bruno",
        "lastName": "Mars"
    }
    ```
    ```json
    {
        "firstName": "Merchant",
        "lastName": "Store"
    }
    ```

### Create 2 Bank accounts


- **POST**
    ```
    http://localhost:8082/api/customers/{customerId}/accounts
    ```
    ```json
    {
        "accountName": "Bruno M",
        "accountType": "PERSONAL"
    }
    ```
    ```json
    {
        "accountName": "Merchant Account",
        "accountType": "BUSINESS"
    }
    ```

### Transfer/ Deposit/ Withdraw

- **POST**
    ```
    http://localhost:8082/api/transfer
    ```
    ```
    http://localhost:8082/api/deposit
    ```
    ```
    http://localhost:8082/api/withdraw
    ```
    ```json
    {
        "fromAccountRef": "BAC-01K8X6PBCG5SCP1SB1Z8HNP2MK",
        "toAccountRef": "BAC-01K8X6N9TBABVZ1GNB3K2ZGPPR",
        "amount": "10",
        "memo": "asd"
    }
    ```
- **Note**
  - `deposit` to `toAccountRef`
  -  `withdraw` from `fromAccountRef`
  -  `memo` is Optional

### Refund
- **POST**
    ```
    http://localhost:8082/api/refund
    ```
    ```json
    {
        "originalTransactionRef": "TX-01K8XEXAHZDBFASEZ26WKG82RN",
        "memo": "bcd"
    }
    ```

- **Note**
  -  `memo` is Optional

### Get all accounts (+ transactions history)
- **GET**
    ```
    http://localhost:8082/api/accounts
    ```

- **GET** by `customerRef`
    ```
    http://localhost:8082/api/customers/{customerRef}/accounts
    ```

### Get 1 account
- **GET**
    ```
    http://localhost:8082/api/accounts
    ```

- **GET** by `customerRef`
    ```
    http://localhost:8082/api/customers/{customerRef}/accounts/{accountRef}
    ```

## Data Integrity & concurrency
- Atomic writes via Spring `@Transactional`
- optimistic locking with `@Version`: writes are protected by the version check at commit/update time.
- pessimistic locking with `@Lock(PESSIMISTIC_WRITE)` repository methods, locked row(s) for update
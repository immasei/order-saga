
# Bank as an External service
## Base url
```
http://localhost:8081/
```

## Routes summary
```
GET    /api/customers
POST   /api/customers
GET    /api/customers/{customerId}

GET    /api/customers/{customerId}/accounts
POST   /api/customers/{customerId}/accounts
GET    /api/customers/{customerId}/accounts/{accountId}

POST   /api/transactions/transfer
POST   /api/transactions/deposit
POST   /api/transactions/withdraw
POST   /api/transactions/refund
```

## Data Integrity & concurrency
- Atomic writes via Spring `@Transactional`
- optimistic locking with `@Version`: writes are protected by the version check at commit/update time.
- pessimistic locking with `@Lock(PESSIMISTIC_WRITE)` repository methods:

    - `findByIdForUpdate(:id)`
    - `findByIdsForUpdateOrdered(:ids)` (sorted to avoid deadlocks)
    - Helper methods: `lockOrThrow(id)` and `lockOrThrow(ids)` to enforce locking + existence checks.

## Todos
    - bank as an external service (this)
        - idempotency
        - retry
        - log
    - bank as an internal service under store/ (PaymentService -talks to Bank API)
# STORE API README

## Overview

This project implements a **store management system** using Spring Boot. It includes:

- Users: `Customer`, `Admin` (inheritance from `User`)
- Warehouses: `Warehouse` → `Warehouse1Stock` & `Warehouse2Stock` (composition & inheritance)
- Products, Orders, and Purchase History management
- REST APIs to perform CRUD operations and manage stock

Polymorphism and inheritance are used to generalize behavior:

- **User → Customer / Admin**
    - `User` is the parent class with fields like `email`, `passwordHash`, `role`
    - `Customer` and `Admin` extend `User` and add specific fields (e.g., `phone`, `address` for Customer)
- **Warehouse → Warehouse1Stock / Warehouse2Stock**
    - `Warehouse` holds generic warehouse information
    - `Warehouse1Stock` & `Warehouse2Stock` extend the concept to track **stock per warehouse**, enabling polymorphic handling of warehouse inventory

---

## API Endpoints

### 0. Auth Management

#### Quick facts
- **Roles**: `ADMIN`, `CUSTOMER`
- **Signup**: Public, `CUSTOMER` only (cannot create `ADMIN`)
- **Admin creation**: `ADMIN` only via `/api/admins`
- **Access token (JWT)**: Returned in response, client must send as `Authorization: Bearer <token>`
  - last 1 day
- **Refresh token (JWT)**: Set as HttpOnly cookie named `refreshToken`
  - Refresh token is not rotated.
  - Refresh cookie path limited to `/api/auth/refresh`
- **Typical errors**: 400 validation, 401 invalid/expired JWT, 403 forbidden, 404 not found, 409 email already in use
- **CustomerController**
  - get/update/delete: hasRole('ADMIN') or @customerSecurity.isAccountOwner(#id)
  - create: hasRole('ADMIN')
- **AdminController**
  - All routes require ADMIN

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

#### Notes to add

- Example of authorisation at controller level: see `controller/CustomerController` (method-level `@PreAuthorize` with owner-or-admin checks).
- Configure public vs private routes in `config/WebSecurityConfig`.

#### Mini routes table

Method | Path | Auth | Description
-------|------|------|-------------
POST | /api/auth/signup | Public | Sign up customer and auto-login. Sets refresh cookie; returns access tokens.
POST | /api/auth/login | Public | Login. Sets refresh cookie; returns access tokens.
POST | /api/auth/refresh | Cookie refreshToken (HttpOnly) | Issues new access token using refresh cookie.
POST | /api/auth/logout | Any logged-in user | Clears refresh cookie.
POST | /api/admins | ADMIN | Create admin account.
GET | /api/admins | ADMIN | List all admins.
GET | /api/admins/{id} | ADMIN | Get admin by ID.
GET | /api/customers/{id} | ADMIN or resource owner | Get customer by ID.
POST | /api/customers | ADMIN | Create customer (admin-created).
PATCH | /api/customers/{id} | ADMIN or resource owner | Partial update of a customer.
DELETE | /api/customers/{id} | ADMIN or resource owner | Delete a customer.
GET | /api/customers | ADMIN | List all customers.

---

#### Signup (create Customer only)

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

#### Login

**Default admin account**

```json
{
  "email": "admin@example.com",
  "password": "supersaiya"
}
```


### 1. User Management

#### Create Admin (admin only)

```POST http://localhost:8080/api/admins```

**Body:**

```json
{
    "user_type": "ADMIN",
    "email": "admin@gmail.com",
    "passwordHash": "123",
    "role": "ADMIN",
    "firstName": "firstName",
    "lastName": "lastName"
}
```

#### Create Customer (admin only)
```POST http://localhost:8080/api/customers```

**Body:**

```json
{
  "user_type": "CUSTOMER",
  "email": "customer@gmail.com",
  "passwordHash": "123",
  "role": "CUSTOMER",
  "firstName": "firstName",
  "lastName": "lastName",
  "phone": "123-456-7890",
  "address": "123 Main St, Springfield, IL"
}
```

### 2. Product Management

#### Create Product
```POST http://localhost:8080/api/products```

**Body:**

```json
{
  "name": "name",
  "description": "description",
  "price": 12
}
```

### 3. Order Management
#### Create Product
```POST http://localhost:8080/api/orders```

Brace urself for this api call yall

**Body:**

```json
{
  "orderNumber": "1",
  "customer": {
    "id": "81382aa7-b744-470d-bca6-19e262a679cb",
    "email": "admin@gmail.com",
    "user_type": "ADMIN"
  },
  "deliveryAddress": "123 Main Street, City, Country",
  "status": "Pending",
  "subTotal": 12,
  "shipping": 3,
  "tax": 1,
  "total": 16,
  "placedAt": "2023-10-17T12:00:00",
  "orderItems": [
    {
      "product": {
        "id": "8c626fa9-bbbe-4b60-bd03-e28d1a48ad96",
        "name": "cupcake"
      },
      "name": "cupcake",
      "unitPrice": 12,
      "quantity": 1,
      "lineTotal": 16
    }
  ]
}

```

### 4. Warehouse Management

#### Create Product
```POST http://localhost:8080/api/warehouses```

**Body:**

```json
{
  "name": "Warehouse 1",
  "location": "Sydney, Australia"
}

```

#### Get Warehouse By ID
```GET http://localhost:8080/api/warehouses/{warehouseId}```

### 5. Warehouse Stock Management


#### Add / Update Stock for Warehouse 1
```POST http://localhost:8080/api/warehouse-stocks/warehouse1```

**Body:**

```json
{
  "id": {
    "warehouseId": "11111111-1111-1111-1111-111111111111",
    "productId": "22222222-2222-2222-2222-222222222222"
  },
  "warehouse": {
    "id": "11111111-1111-1111-1111-111111111111"
  },
  "product": {
    "id": "22222222-2222-2222-2222-222222222222"
  },
  "stock": 100
}


```

### 5. Product Purchase History
```GET http://localhost:8080/api/product-purchase-history/{productId}```


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




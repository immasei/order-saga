-- =====================================================================
-- V1__init.sql  (Flyway)
-- Initial schema for Store app (PostgreSQL)
-- =====================================================================

-- ======================
-- Tables
-- ======================

CREATE TABLE IF NOT EXISTS public.users (
    user_type   varchar(31)    NOT NULL,
    id          uuid           NOT NULL,
    email       varchar(255)   NOT NULL,
    first_name  varchar(100),
    last_name   varchar(100),
    password    varchar(255)   NOT NULL,
    role        varchar(255)   NOT NULL,
    version     bigint
);

CREATE TABLE IF NOT EXISTS public.admin (
    id uuid NOT NULL
);

CREATE TABLE IF NOT EXISTS public.customer (
    address        varchar(255),
    phone          varchar(30),
    bank_account_ref varchar(40),
    id      uuid NOT NULL
);

CREATE TABLE IF NOT EXISTS public.products (
    id            uuid          NOT NULL,
    description   text,
    price         numeric(15,2) NOT NULL,
    product_code  varchar(30)   NOT NULL,
    product_name  varchar(200)  NOT NULL
);

CREATE TABLE IF NOT EXISTS public.warehouses (
    id              uuid         NOT NULL,
    location        varchar(100) NOT NULL,
    warehouse_code  varchar(20)  NOT NULL,
    warehouse_name  varchar(100) NOT NULL
);

-- Parent is partitioned by warehouse_id
CREATE TABLE IF NOT EXISTS public.warehouse_stock (
    warehouse_id uuid    NOT NULL,
    product_id   uuid    NOT NULL,
    on_hand      integer NOT NULL,
    reserved     integer NOT NULL
) PARTITION BY LIST (warehouse_id);

CREATE TABLE IF NOT EXISTS public.orders (
    id                   uuid          NOT NULL,
    delivery_address     varchar(255)  NOT NULL,
    order_number         varchar(30)   NOT NULL,
    placed_at            timestamp(6)  NOT NULL,
    shipping             numeric(15,2) NOT NULL,
    status               varchar(100)   NOT NULL,
    sub_total            numeric(15,2) NOT NULL,
    tax                  numeric(15,2) NOT NULL,
    total                numeric(15,2) NOT NULL,
    customer_id          uuid          NOT NULL,
    idempotency_key      varchar(80)   NOT NULL,
    payment_account_ref  varchar(100)  NOT NULL,
    delivery_tracking_id uuid
);

CREATE TABLE IF NOT EXISTS public.order_item (
    id                        uuid           NOT NULL,
    line_total                numeric(15,2)  NOT NULL,
    product_code_at_purchase  varchar(30)    NOT NULL,
    product_name_at_purchase  varchar(200)   NOT NULL,
    quantity                  integer        NOT NULL,
    unit_price                numeric(15,2)  NOT NULL,
    order_id                  uuid           NOT NULL,
    product_id                uuid           NOT NULL
);

CREATE TABLE IF NOT EXISTS public.payments (
    id               uuid           NOT NULL,
    amount           numeric(15,2)  NOT NULL,
    idempotency_key  varchar(80)    NOT NULL,
    order_id         uuid           NOT NULL,
    provider_txn_id  varchar(30),
    refunded_total   numeric(15,2)  NOT NULL,
    status           varchar(20)    NOT NULL
);

CREATE TABLE IF NOT EXISTS public.outbox (
    id uuid NOT NULL,
    aggregate_id     varchar(255)   NOT NULL,
    aggregate_type   varchar(255)   NOT NULL,
    event_type       varchar(255)   NOT NULL,
    topic            varchar(255)   NOT NULL,
    payload          jsonb          NOT NULL,
    status           varchar(255)   NOT NULL,
    attempts         integer        NOT NULL,
    created_at       timestamp(6)   NOT NULL
);

CREATE TABLE IF NOT EXISTS public.email_record (
    id UUID NOT NULL,
    order_id uuid NOT NULL,
    to_address VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    body VARCHAR(512),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);


-- ======================
-- Primary Keys & Unique
-- ======================

ALTER TABLE public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE public.admin
    ADD CONSTRAINT admin_pkey PRIMARY KEY (id);

ALTER TABLE public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (id);

ALTER TABLE public.products
    ADD CONSTRAINT products_pkey PRIMARY KEY (id);

ALTER TABLE public.warehouses
    ADD CONSTRAINT warehouses_pkey PRIMARY KEY (id);

ALTER TABLE public.warehouse_stock
    ADD CONSTRAINT warehouse_stock_pk PRIMARY KEY (warehouse_id, product_id);

ALTER TABLE public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);

ALTER TABLE public.order_item
    ADD CONSTRAINT order_item_pkey PRIMARY KEY (id);

ALTER TABLE public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (id);

-- Unique constraints
ALTER TABLE public.orders
    ADD CONSTRAINT idx_orders_order_number UNIQUE (order_number);

ALTER TABLE public.products
    ADD CONSTRAINT idx_product_code UNIQUE (product_code);

ALTER TABLE public.warehouses
    ADD CONSTRAINT idx_warehouse_code UNIQUE (warehouse_code);

ALTER TABLE public.order_item
    ADD CONSTRAINT uk_orderitem_order_product UNIQUE (order_id, product_id);

ALTER TABLE public.payments
    ADD CONSTRAINT uk_payment_order_id UNIQUE (order_id);

ALTER TABLE public.payments
    ADD CONSTRAINT uk_payments_provider_txn UNIQUE (provider_txn_id);

ALTER TABLE public.users
    ADD CONSTRAINT uk_users_email UNIQUE (email);

ALTER TABLE public.orders
    ADD CONSTRAINT uk_orders_order_number UNIQUE (order_number);

ALTER TABLE public.orders
    ADD CONSTRAINT uk_orders_idempotency_key UNIQUE (idempotency_key);

-- ======================
-- Indexes
-- ======================

CREATE INDEX IF NOT EXISTS idx_orderitem_product ON public.order_item (product_id);
CREATE INDEX IF NOT EXISTS idx_payment_order_id  ON public.payments   (order_id);

-- Partitioned indexes on parent create matching indexes on partitions
CREATE INDEX IF NOT EXISTS idx_ws_product ON public.warehouse_stock (product_id);
CREATE INDEX IF NOT EXISTS idx_ws_wh      ON public.warehouse_stock (warehouse_id);

-- ======================
-- Foreign Keys (stable names; duplicates removed)
-- ======================

-- Subclass tables referencing users
ALTER TABLE public.admin
    ADD CONSTRAINT fk_admin_user
    FOREIGN KEY (id) REFERENCES public.users(id);

ALTER TABLE public.customer
    ADD CONSTRAINT fk_customer_user
    FOREIGN KEY (id) REFERENCES public.users(id);

-- Orders ← Customer
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_customer
    FOREIGN KEY (customer_id) REFERENCES public.customer(id);

-- Order item ← Orders / Products
ALTER TABLE public.order_item
    ADD CONSTRAINT fk_order_item_order
    FOREIGN KEY (order_id) REFERENCES public.orders(id);

ALTER TABLE public.order_item
    ADD CONSTRAINT fk_order_item_product
    FOREIGN KEY (product_id) REFERENCES public.products(id);

-- Payments ← Orders
ALTER TABLE public.payments
    ADD CONSTRAINT fk_payments_order
    FOREIGN KEY (order_id) REFERENCES public.orders(id);

-- Warehouse stock ← Warehouses / Products
ALTER TABLE public.warehouse_stock
    ADD CONSTRAINT fk_ws_wh
    FOREIGN KEY (warehouse_id) REFERENCES public.warehouses(id);

ALTER TABLE public.warehouse_stock
    ADD CONSTRAINT fk_ws_prod
    FOREIGN KEY (product_id) REFERENCES public.products(id);

-- ======================
-- (Optional) Partitions
-- Create child partitions when you create a warehouse, OR define a default:
-- CREATE TABLE IF NOT EXISTS public.warehouse_stock_default
--   PARTITION OF public.warehouse_stock DEFAULT;
-- ======================

-- =====================================================================
-- V2__inventory_reservations.sql  (Flyway)
-- Inventory reservation tables used to track per-order warehouse commits
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.inventory_reservation (
    id               uuid          NOT NULL,
    order_number     varchar(30)   NOT NULL,
    status           varchar(20)   NOT NULL,
    idempotency_key  varchar(80)   NOT NULL,
    failure_reason   text,
    created_at       timestamp(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamp(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT inventory_reservation_pkey PRIMARY KEY (id),
    CONSTRAINT uk_inventory_reservation_order UNIQUE (order_number),
    CONSTRAINT uk_inventory_reservation_idempo UNIQUE (idempotency_key)
);

CREATE TABLE IF NOT EXISTS public.inventory_reservation_item (
    id              uuid   NOT NULL,
    reservation_id  uuid   NOT NULL,
    warehouse_id    uuid   NOT NULL,
    product_id      uuid   NOT NULL,
    quantity        integer NOT NULL,

    CONSTRAINT inventory_reservation_item_pkey PRIMARY KEY (id),
    CONSTRAINT fk_inventory_res_item_reservation FOREIGN KEY (reservation_id)
        REFERENCES public.inventory_reservation (id) ON DELETE CASCADE,
    CONSTRAINT fk_inventory_res_item_warehouse FOREIGN KEY (warehouse_id)
        REFERENCES public.warehouses (id),
    CONSTRAINT fk_inventory_res_item_product FOREIGN KEY (product_id)
        REFERENCES public.products (id),
    CONSTRAINT ck_inventory_res_item_quantity CHECK (quantity > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_res_items_res_wh_prod
    ON public.inventory_reservation_item (reservation_id, warehouse_id, product_id);

CREATE INDEX IF NOT EXISTS idx_inventory_res_item_reservation
    ON public.inventory_reservation_item (reservation_id);




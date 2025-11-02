-- =====================================================================
-- V3__product_purchase_history.sql  (Flyway)
-- Tracks how many times each product has been purchased
-- =====================================================================

CREATE TABLE IF NOT EXISTS public.product_purchase_history (
    id             uuid           NOT NULL,
    product_id     uuid           NOT NULL,
    purchase_count integer        NOT NULL,

    CONSTRAINT product_purchase_history_pkey PRIMARY KEY (id),
    CONSTRAINT fk_pph_product FOREIGN KEY (product_id)
        REFERENCES public.products (id) ON DELETE CASCADE,
    CONSTRAINT uk_pph_product UNIQUE (product_id),
    CONSTRAINT ck_pph_purchase_count CHECK (purchase_count >= 0)
);

CREATE INDEX IF NOT EXISTS idx_pph_product
    ON public.product_purchase_history (product_id);


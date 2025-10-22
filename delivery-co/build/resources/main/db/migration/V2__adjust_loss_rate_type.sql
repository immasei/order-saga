ALTER TABLE delivery_order
    ALTER COLUMN loss_rate TYPE DOUBLE PRECISION USING loss_rate::double precision,
    ALTER COLUMN loss_rate SET DEFAULT 0.05;

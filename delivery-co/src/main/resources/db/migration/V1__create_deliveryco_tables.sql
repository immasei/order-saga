CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE delivery_order (
    id UUID PRIMARY KEY,
    external_order_id VARCHAR(64) NOT NULL UNIQUE,
    customer_id VARCHAR(64) NOT NULL,
    pickup_warehouse_id VARCHAR(64),
    pickup_address TEXT,
    dropoff_address TEXT,
    contact_email VARCHAR(255),
    current_status VARCHAR(32) NOT NULL,
    loss_rate NUMERIC(4,3) NOT NULL DEFAULT 0.050,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    acknowledged_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE delivery_item (
    id UUID PRIMARY KEY,
    delivery_order_id UUID NOT NULL REFERENCES delivery_order(id) ON DELETE CASCADE,
    sku VARCHAR(64) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
    fulfillment_status VARCHAR(32) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_delivery_item_order ON delivery_item (delivery_order_id);

CREATE TABLE delivery_status_event (
    id UUID PRIMARY KEY,
    delivery_order_id UUID NOT NULL REFERENCES delivery_order(id) ON DELETE CASCADE,
    correlation_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason TEXT,
    payload JSONB,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_status_event_order ON delivery_status_event (delivery_order_id);

CREATE TABLE delivery_job (
    id UUID PRIMARY KEY,
    delivery_order_id UUID NOT NULL REFERENCES delivery_order(id) ON DELETE CASCADE,
    job_type VARCHAR(32) NOT NULL,
    run_at TIMESTAMP WITH TIME ZONE NOT NULL,
    attempt INTEGER NOT NULL DEFAULT 0,
    state VARCHAR(16) NOT NULL,
    lock_owner VARCHAR(128),
    locked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_delivery_job_state_run ON delivery_job (state, run_at);
CREATE INDEX idx_delivery_job_order ON delivery_job (delivery_order_id);

CREATE TABLE delivery_outbox (
    id UUID PRIMARY KEY,
    delivery_order_id UUID REFERENCES delivery_order(id) ON DELETE SET NULL,
    event_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE,
    publish_state VARCHAR(16) NOT NULL,
    failure_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_delivery_outbox_state ON delivery_outbox (publish_state, created_at);

CREATE TABLE delivery_incident (
    id UUID PRIMARY KEY,
    delivery_item_id UUID NOT NULL REFERENCES delivery_item(id) ON DELETE CASCADE,
    incident_type VARCHAR(32) NOT NULL,
    notes TEXT,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_delivery_incident_item ON delivery_incident (delivery_item_id);

CREATE TABLE delivery_worker_heartbeat (
    node_id UUID PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    last_seen TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(16) NOT NULL
);

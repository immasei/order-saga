-- Create base table for storing sent/queued emails
-- Compatible with PostgreSQL and H2
CREATE TABLE IF NOT EXISTS email_message (
  id UUID PRIMARY KEY,
  order_id UUID NULL,
  to_address VARCHAR(255) NOT NULL,
  external_order_id VARCHAR(64),
  subject VARCHAR(255) NOT NULL,
  body TEXT NOT NULL,
  status VARCHAR(32) NOT NULL,
  message_type VARCHAR(40),
  created_at TIMESTAMP NOT NULL,
  sent_at TIMESTAMP NULL
);


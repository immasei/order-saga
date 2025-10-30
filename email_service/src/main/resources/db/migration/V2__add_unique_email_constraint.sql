-- Ensure one email per (toAddress, externalOrderId, messageType)
-- Works on Postgres and H2
CREATE UNIQUE INDEX IF NOT EXISTS ux_email_dedupe
  ON email_message(to_address, external_order_id, message_type);


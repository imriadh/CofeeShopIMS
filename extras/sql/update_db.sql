-- Run this script to update your existing database schema
ALTER TABLE orders
ADD payment_method VARCHAR2(20);
ALTER TABLE orders
ADD card_number VARCHAR2(20);
-- Add Service Boy User if not exists (this might fail if constraint violation, but good for update)
-- Better to run this manually or handle in app logic, but for this script:
INSERT INTO users (username, password, role)
VALUES ('service', 'service123', 'STAFF');
COMMIT;
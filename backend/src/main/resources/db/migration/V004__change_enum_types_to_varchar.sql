ALTER TABLE service_requests
ALTER COLUMN status TYPE VARCHAR(20) USING status::VARCHAR;

ALTER TABLE service_requests
ALTER COLUMN priority TYPE VARCHAR(20) USING priority::VARCHAR;

ALTER TABLE service_requests
ALTER COLUMN category TYPE VARCHAR(20) USING category::VARCHAR;
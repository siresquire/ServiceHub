--==========================================================================
-- FIX: Reset service_requests sequence to avoid duplicate key violations
-- The sequence can get out of sync when data is inserted manually or
-- when the sequence value doesn't match the max existing ID
--==========================================================================

-- Reset the sequence to the maximum existing ID + 1
-- This ensures new inserts won't conflict with existing rows
SELECT setval(
    'service_requests_id_seq',
    COALESCE((SELECT MAX(id) FROM service_requests), 0) + 1,
    false
);


-- Insert the first test admin
INSERT INTO users (id, email, password, role)
VALUES (
           '223e4567-e89b-12d3-a456-426614174006',
           'testuser@test.com',
           '$2b$12$7hoRZfJrRKD2nIm2vHLs7OBETy.LWenXXMLKf99W8M4PUwO6KB7fu',
           'ADMIN'
       )
    ON CONFLICT DO NOTHING;

-- Insert the second test admin
INSERT INTO users (id, email, password, role)
VALUES (
           '11111111-1111-1111-1111-111111111111',
           'test@example.com',
           '$2b$10$ZCO0Kp8TgtP2Dh398I9.O.o0a6xtUHZYKpEgZ5k1KA3fXxPW7byGe',
           'ADMIN'
       )
    ON CONFLICT DO NOTHING;
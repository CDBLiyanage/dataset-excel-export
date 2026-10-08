CREATE TABLE IF NOT EXISTS dataset (
    id      BIGINT       PRIMARY KEY,
    name    VARCHAR(100) NOT NULL,
    email   VARCHAR(150) NOT NULL,
    age     INT,
    country VARCHAR(50)
);
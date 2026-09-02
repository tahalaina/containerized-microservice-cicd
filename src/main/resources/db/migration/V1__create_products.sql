CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price > 0)
);

INSERT INTO products (name, price) VALUES ('Starter product', 9.99);

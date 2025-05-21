CREATE TABLE inventario (
    id BIGSERIAL PRIMARY KEY,
    producto_id BIGINT UNIQUE NOT NULL,
    cantidad INT NOT NULL
);
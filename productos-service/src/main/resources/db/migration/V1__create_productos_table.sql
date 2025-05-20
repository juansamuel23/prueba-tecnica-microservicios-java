CREATE TABLE productos (
    id VARCHAR(255) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    precio NUMERIC(10, 2) NOT NULL -- Usamos NUMERIC para BigDecimal
);
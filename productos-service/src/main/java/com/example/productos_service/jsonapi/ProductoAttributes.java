package com.example.productos_service.jsonapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Importa BigDecimal si tu precio es de ese tipo

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoAttributes {
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
}

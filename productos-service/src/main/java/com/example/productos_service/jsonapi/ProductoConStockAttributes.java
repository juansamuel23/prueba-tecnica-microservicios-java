package com.example.productos_service.jsonapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoConStockAttributes {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stockDisponible; // Este campo viene del servicio de inventario
}

package com.example.inventario_service.jsonapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioAttributes {
    private Long productoId;
    private Integer cantidad;
}

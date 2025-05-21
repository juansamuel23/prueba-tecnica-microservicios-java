package com.example.inventario_service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; // Importa BigDecimal si los precios son así

// Clase principal que representa la respuesta JSON:API para un solo recurso
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private DataBlock data; // El bloque 'data' que contiene el recurso principal

    // Clase anidada para el bloque 'data' de un recurso JSON:API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataBlock {
        private String id; // El ID del recurso como String
        private String type; // El tipo de recurso (ej. "productos")
        private Attributes attributes; // Los atributos del recurso

        // Opcional: Si hubiera relaciones (ej. con categorías), las incluirías aquí
        // private Relationships relationships;
    }

    // Clase anidada para los 'attributes' del recurso Producto
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attributes {
        private String nombre;
        private String descripcion; // Si tu producto tiene descripción
        private BigDecimal precio; // Asegúrate de que el tipo coincida con tu entidad Producto
        // Si tu modelo de producto tiene más campos, añádelos aquí.
    }
}

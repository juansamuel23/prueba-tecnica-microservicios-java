package com.example.productos_service.client.model;

public class InventarioResponse {

    private Long id;         // ID interno del registro de inventario
    private Long productoId; // ID del producto al que se refiere este inventario
    private Integer cantidad; // Cantidad de stock

    // Constructor vacío (necesario para la deserialización de JSON)
    public InventarioResponse() {
    }

    // Constructor con todos los campos (útil para crear instancias)
    public InventarioResponse(Long id, Long productoId, Integer cantidad) {
        this.id = id;
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "InventarioResponse{" +
                "id=" + id +
                ", productoId=" + productoId +
                ", cantidad=" + cantidad +
                '}';
    }
}


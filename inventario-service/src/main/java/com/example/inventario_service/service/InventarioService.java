package com.example.inventario_service.service;

import com.example.inventario_service.dto.InventarioRequest;
import com.example.inventario_service.dto.InventarioResponse;
import com.example.inventario_service.model.Inventario;

import java.util.Optional;

//Definición de la interfaz de servicio de inventario
public interface InventarioService {

    Inventario saveInventario(Inventario inventario);

    Iterable<Inventario> getAllInventario();

    Optional<Inventario> getInventarioByProductoId(Long productoId);

    Inventario reduceStock(Long productoId, Integer cantidad);
}

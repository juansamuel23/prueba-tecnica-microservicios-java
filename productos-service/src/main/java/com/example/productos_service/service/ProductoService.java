package com.example.productos_service.service;

import com.example.productos_service.model.Producto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductoService {

    Producto crearProducto(Producto producto);

    Producto obtenerProducto(String id);

    List<Producto> listarProductos(Pageable pageable);

    Producto actualizarProducto(String id, Producto producto);

    void eliminarProducto(String id);

}

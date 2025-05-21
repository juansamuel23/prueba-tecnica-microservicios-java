package com.example.productos_service.service;

import com.example.productos_service.model.Producto;
import com.example.productos_service.model.ProductoConStockDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    // Método para guardar/actualizar un producto (y posiblemente inicializar inventario)
    Producto saveProducto(Producto producto);

    // Método para obtener un producto por ID y su stock (versión combinada)
    Mono<ProductoConStockDTO> getProductoByIdWithStock(Long id);

    // Método para obtener un producto por ID (versión básica sin stock)
    Optional<Producto> getProductoById(Long id);

    // Método para obtener todos los productos
    Page<Producto> getAllProductos(Pageable pageable);

    // Método para eliminar un producto por ID
    void deleteProducto(Long id);

    // Método para reducir el stock de un producto, retorna el DTO combinado
    Mono<ProductoConStockDTO> reducirStockProducto(Long productoId, Integer cantidad);

}

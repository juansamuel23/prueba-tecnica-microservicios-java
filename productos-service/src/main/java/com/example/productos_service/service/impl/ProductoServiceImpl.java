package com.example.productos_service.service.impl;

import com.example.productos_service.client.InventarioServiceClient;
import com.example.productos_service.model.Producto;
import com.example.productos_service.model.ProductoConStockDTO;
import com.example.productos_service.repository.ProductoRepository;
import com.example.productos_service.service.ProductoService;
import com.example.productos_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.util.List;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioServiceClient inventarioServiceClient; // Inyecta el cliente del servicio de Inventario

    /**
     * Guarda un producto en la base de datos y, si es nuevo, inicializa su inventario en el servicio de Inventario.
     * @param producto El objeto Producto a guardar.
     * @return El objeto Producto guardado.
     */
    public Producto saveProducto(Producto producto) {
        Producto savedProducto = productoRepository.save(producto);

        // Almacena en la base de datos de productos. Luego, intenta inicializar el inventario.
        // La llamada a crearInventario es asíncrona y no bloquea el flujo principal.
        inventarioServiceClient.crearInventario(savedProducto.getId(), 0) // Inicializa con 0 unidades
                .subscribe(
                        inventarioResponse -> System.out.println("Inventario creado/actualizado para producto: " + inventarioResponse.getProductoId() + " con cantidad: " + inventarioResponse.getCantidad()),
                        error -> System.err.println("Error al crear/actualizar inventario para producto " + savedProducto.getId() + ": " + error.getMessage())
                );

        return savedProducto;
    }

    /**
     * Obtiene un producto por su ID y combina su información con el stock disponible del servicio de Inventario.
     * @param id El ID del producto.
     * @return Mono<ProductoConStockDTO> que contiene el producto y su stock, o Mono.empty() si el producto no existe.
     */
    public Mono<ProductoConStockDTO> getProductoByIdWithStock(Long id) {
        // Obtener el producto de la base de datos de productos.
        // fromCallable y subscribeOn son usados para envolver una operación bloqueante (findById) en un flujo reactivo
        // y ejecutarla en un Scheduler diferente para no bloquear el hilo principal de Netty (si usas WebFlux).
        Mono<Producto> productoMono = Mono.fromCallable(() -> productoRepository.findById(id).orElse(null))
                .subscribeOn(Schedulers.boundedElastic());

        return productoMono.flatMap(producto -> {
            if (producto == null) {
                return Mono.empty(); // Si el producto no se encuentra en la DB de productos
            }

            ProductoConStockDTO dto = new ProductoConStockDTO(producto);

            // Llamar al servicio de inventario para obtener el stock
            return inventarioServiceClient.obtenerInventarioPorProductoId(producto.getId())
                    .map(inventarioResponse -> {
                        // Si se encuentra inventario, asigna la cantidad al DTO
                        dto.setStockDisponible(inventarioResponse.getCantidad());
                        return dto;
                    })
                    // Si el inventario no se encuentra (WebClient devuelve Mono.empty() para 404),
                    // o si hay un error en la llamada, asigna stock 0 y devuelve el DTO.
                    .defaultIfEmpty(dto) // En caso de Mono.empty() del cliente
                    .onErrorResume(e -> { // En caso de error en la llamada HTTP
                        System.err.println("Advertencia: No se pudo obtener inventario para producto " + producto.getId() + ": " + e.getMessage());
                        dto.setStockDisponible(0); // Valor por defecto si no se puede obtener el stock
                        return Mono.just(dto);
                    });
        });
    }

    /**
     * Reduce la cantidad de stock de un producto llamando al servicio de Inventario,
     * y luego devuelve la información actualizada del producto con el nuevo stock.
     * @param productoId El ID del producto cuyo stock se va a reducir.
     * @param cantidad La cantidad a reducir.
     * @return Mono<ProductoConStockDTO> con el producto y su stock actualizado.
     */
    public Mono<ProductoConStockDTO> reducirStockProducto(Long productoId, Integer cantidad) {
        return inventarioServiceClient.reducirStock(productoId, cantidad) // Llama al servicio de Inventario para reducir stock
                .flatMap(inventarioResponse -> {
                    // Si la reducción de stock fue exitosa, obtenemos el producto con el stock actualizado
                    return getProductoByIdWithStock(productoId);
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Producto o inventario no encontrado durante la reducción de stock.")))
                .onErrorResume(e -> {
                    // Captura errores como stock insuficiente o problemas de comunicación
                    System.err.println("Error al reducir stock para producto " + productoId + ": " + e.getMessage());
                    return Mono.error(new IllegalArgumentException(e.getMessage())); // Propaga una excepción más amigable
                });
    }

    // --- Otros métodos básicos del CRUD de Productos ---

    public Optional<Producto> getProductoById(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Obtiene todos los productos con paginación.
     * @param pageable Objeto Pageable que contiene la información de paginación (número de página, tamaño de página, ordenación).
     * @return Un objeto Page<Producto> que contiene la lista de productos para la página solicitada,
     * además de información sobre la paginación (total de elementos, total de páginas, etc.).
     */
    @Override
    public Page<Producto> getAllProductos(Pageable pageable) { // <-- Modificado
        return productoRepository.findAll(pageable); // <-- Usa el método findAll(Pageable) del JpaRepository
    }

    @Override
    public void deleteProducto(Long id) {
        // 1. Busca el producto por ID. Si no existe, lanza ResourceNotFoundException.
        productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));

        // 2. Si el producto existe, procede a eliminarlo.
        productoRepository.deleteById(id);
    }
}

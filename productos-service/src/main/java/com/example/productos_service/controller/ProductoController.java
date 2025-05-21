package com.example.productos_service.controller;


import com.example.productos_service.model.Producto;
import com.example.productos_service.model.ProductoConStockDTO;
import com.example.productos_service.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;
import java.util.Optional;


@RestController
@RequestMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductoController {

    @Autowired
    private ProductoService productoService; // Inyecta el servicio de Productos

    /**
     * Crea un nuevo producto.
     * POST /api/productos
     * @param producto El objeto Producto enviado en el cuerpo de la solicitud.
     * @return ResponseEntity con el producto creado y el estado HTTP 201 CREATED.
     */
    @PostMapping
    public ResponseEntity<Producto> createProducto(@RequestBody Producto producto) {
        Producto savedProducto = productoService.saveProducto(producto);
        return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
    }

    /**
     * Obtiene un producto por su ID, incluyendo la información de stock.
     * GET /api/productos/{id}
     * @param id El ID del producto.
     * @return Mono<ResponseEntity<ProductoConStockDTO>> con el producto y su stock, o 404 NOT_FOUND.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductoConStockDTO>> getProductoById(@PathVariable Long id) {
        return productoService.getProductoByIdWithStock(id)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Obtiene un producto por su ID, sin incluir la información de stock (solo el producto base).
     * GET /api/productos/basic/{id}
     * @param id El ID del producto.
     * @return ResponseEntity con el producto, o 404 NOT_FOUND.
     */
    @GetMapping("/basic/{id}")
    public ResponseEntity<Producto> getProductoByIdBasic(@PathVariable Long id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Obtiene todos los productos con paginación.
     * GET /api/productos?page=0&size=10&sort=nombre,asc
     * @param pageable Objeto Pageable inyectado automáticamente por Spring.
     * Puedes usar @PageableDefault para definir valores por defecto.
     * @return ResponseEntity con una página de productos y el estado HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<Page<Producto>> getAllProductos(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) { // <-- Modificado
        Page<Producto> productosPage = productoService.getAllProductos(pageable); // <-- Llama al servicio con Pageable
        return new ResponseEntity<>(productosPage, HttpStatus.OK);
    }

    /**
     * Actualiza un producto existente.
     * PUT /api/productos/{id}
     * @param id El ID del producto a actualizar.
     * @param producto El objeto Producto con los datos actualizados.
     * @return ResponseEntity con el producto actualizado, o 404 NOT_FOUND si no existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        Optional<Producto> existingProducto = productoService.getProductoById(id);
        if (existingProducto.isPresent()) {
            producto.setId(id); // Asegura que el ID de la URL se use
            Producto updatedProducto = productoService.saveProducto(producto);
            return new ResponseEntity<>(updatedProducto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Elimina un producto por su ID.
     * DELETE /api/productos/{id}
     * @param id El ID del producto a eliminar.
     * @return ResponseEntity con 204 NO_CONTENT si se elimina, o 404 NOT_FOUND si no existe.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        Optional<Producto> existingProducto = productoService.getProductoById(id);
        if (existingProducto.isPresent()) {
            productoService.deleteProducto(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Reduce el stock de un producto específico.
     * PUT /api/productos/{id}/reducir-stock/{cantidad}
     * @param id El ID del producto.
     * @param cantidad La cantidad a reducir.
     * @return Mono<ResponseEntity<ProductoConStockDTO>> con el producto y su stock actualizado,
     * o 400 BAD_REQUEST si hay un problema (ej. stock insuficiente), o 404 NOT_FOUND.
     */
    @PutMapping("/{id}/reducir-stock/{cantidad}")
    public Mono<ResponseEntity<ProductoConStockDTO>> reducirStockProducto(@PathVariable Long id, @PathVariable Integer cantidad) {
        return productoService.reducirStockProducto(id, cantidad)
                .map(dto -> new ResponseEntity<>(dto, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)) // Si el producto no se encuentra o el inventario no devuelve nada
                .onErrorResume(IllegalArgumentException.class, e -> {
                    // Captura errores específicos como "stock insuficiente"
                    System.err.println("Error de cliente al reducir stock para producto " + id + ": " + e.getMessage());
                    return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                })
                .onErrorResume(e -> {
                    // Captura cualquier otra excepción no esperada
                    System.err.println("Error inesperado al reducir stock para producto " + id + ": " + e.getMessage());
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
}

package com.example.productos_service.controller;


import com.example.productos_service.jsonapi.ProductoConStockAttributes;
import com.example.productos_service.model.Producto;
import com.example.productos_service.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import com.example.productos_service.jsonapi.JsonApiData;
import com.example.productos_service.jsonapi.JsonApiResponse;
import com.example.productos_service.jsonapi.ProductoAttributes;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping(value = "/api/productos", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @Operation(summary = "Crea un nuevo producto", description = "Permite registrar un nuevo producto en el sistema, inicializando su stock en inventario.")
    @ApiResponse(responseCode = "201", description = "Producto creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    public ResponseEntity<JsonApiResponse<ProductoAttributes>> createProducto(@RequestBody Producto producto) {
        Producto savedProducto = productoService.saveProducto(producto);

        // Convertir la entidad Producto a JsonApiData<ProductoAttributes>
        ProductoAttributes attributes = new ProductoAttributes(
                savedProducto.getNombre(),
                savedProducto.getDescripcion(),
                savedProducto.getPrecio()
        );
        JsonApiData<ProductoAttributes> data = new JsonApiData<>(
                savedProducto.getId().toString(), // El ID debe ser String en JSON API
                "productos", // Tipo de recurso
                attributes
        );

        // Envolver en JsonApiResponse
        JsonApiResponse<ProductoAttributes> response = new JsonApiResponse<>(data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene un producto por su ID, incluyendo la información de stock.
     * GET /api/productos/{id}
     * @param id El ID del producto.
     * @return Mono<ResponseEntity<ProductoConStockDTO>> con el producto y su stock, o 404 NOT_FOUND.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por su ID", description = "Recupera los detalles de un producto específico, sin información de stock.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public ResponseEntity<JsonApiResponse<ProductoAttributes>> getProductoById(@PathVariable Long id) {
        Optional<Producto> productoOptional = productoService.getProductoById(id);

        return productoOptional.map(producto -> {
            // Convertir la entidad Producto a JsonApiData<ProductoAttributes>
            ProductoAttributes attributes = new ProductoAttributes(
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio()
            );
            JsonApiData<ProductoAttributes> data = new JsonApiData<>(
                    producto.getId().toString(), // El ID debe ser String
                    "productos", // Tipo de recurso
                    attributes
            );
            return new ResponseEntity<>(new JsonApiResponse<>(data), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
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
    @Operation(summary = "Lista todos los productos", description = "Recupera una lista paginada de todos los productos disponibles.")
    @ApiResponse(responseCode = "200", description = "Lista de productos recuperada")
    public ResponseEntity<JsonApiResponse<ProductoAttributes>> getAllProductos(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {

        Page<Producto> productosPage = productoService.getAllProductos(pageable);

        // Convertir la lista de entidades Producto a una lista de JsonApiData<ProductoAttributes>
        List<JsonApiData<ProductoAttributes>> dataList = productosPage.getContent().stream()
                .map(producto -> new JsonApiData<>(
                        producto.getId().toString(), // ID como String
                        "productos", // Tipo de recurso
                        new ProductoAttributes(producto.getNombre(), producto.getDescripcion(), producto.getPrecio())
                ))
                .collect(Collectors.toList());

        // Envolver la lista en JsonApiResponse
        JsonApiResponse<ProductoAttributes> response = new JsonApiResponse<>(dataList);

        // Opcional: Si quieres añadir metadatos de paginación al JsonApiResponse
        // JsonApiMeta meta = new JsonApiMeta();
        // meta.setTotalPages(productosPage.getTotalPages());
        // meta.setTotalElements(productosPage.getTotalElements());
        // response.setMeta(meta);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Actualiza un producto existente.
     * PUT /api/productos/{id}
     * @param id El ID del producto a actualizar.
     * @param producto El objeto Producto con los datos actualizados.
     * @return ResponseEntity con el producto actualizado, o 404 NOT_FOUND si no existe.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto existente", description = "Modifica los detalles de un producto dado su ID.")
    @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado") // Si tu lógica maneja 404 para update
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    public ResponseEntity<JsonApiResponse<ProductoAttributes>> updateProducto(@PathVariable Long id, @RequestBody Producto producto) {
        // Asegúrate de que el ID del producto que llega en el body se establezca
        // para que saveProducto lo use como actualización
        producto.setId(id);
        Producto updatedProducto = productoService.saveProducto(producto); // saveProducto maneja tanto create como update

        // Convertir la entidad Producto a JsonApiData<ProductoAttributes>
        ProductoAttributes attributes = new ProductoAttributes(
                updatedProducto.getNombre(),
                updatedProducto.getDescripcion(),
                updatedProducto.getPrecio()
        );
        JsonApiData<ProductoAttributes> data = new JsonApiData<>(
                updatedProducto.getId().toString(), // El ID debe ser String en JSON API
                "productos", // Tipo de recurso
                attributes
        );

        JsonApiResponse<ProductoAttributes> response = new JsonApiResponse<>(data);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
     * PUT /api/productos/{id}/reducir-stock/{cantidad}}
     *
     * @param cantidad La cantidad a reducir.
     * @return Mono<ResponseEntity<ProductoConStockDTO>> con el producto y su stock actualizado,
     * o 400 BAD_REQUEST si hay un problema (ej. stock insuficiente), o 404 NOT_FOUND.
     */
    @PutMapping("/{productoId}/reducir-stock/{cantidad}")
    @Operation(summary = "Reduce el stock de un producto", description = "Decrementa la cantidad disponible de un producto, simulando una compra.")
    @ApiResponse(responseCode = "200", description = "Stock reducido exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. stock insuficiente)")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public Mono<ResponseEntity<JsonApiResponse<ProductoConStockAttributes>>> reducirStockProducto(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        return productoService.reducirStockProducto(productoId, cantidad)
                .map(dto -> {
                    ProductoConStockAttributes attributes = new ProductoConStockAttributes(
                            dto.getNombre(),
                            dto.getDescripcion(),
                            dto.getPrecio(),
                            dto.getStockDisponible()
                    );
                    JsonApiData<ProductoConStockAttributes> data = new JsonApiData<>(
                            dto.getId().toString(),
                            "productos-con-stock", // El mismo tipo que para el GET con stock
                            attributes
                    );
                    return new ResponseEntity<>(new JsonApiResponse<>(data), HttpStatus.OK);
                })
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND)) // En caso de que el mono esté vacío
                .onErrorResume(e -> { // Manejo de errores para stock insuficiente
                    if (e instanceof IllegalArgumentException) {
                        // Puedes crear una estructura JsonApiError si quieres ser más estricto con el estándar
                        return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
                    }
                    return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }


    @GetMapping("/{id}/with-stock")
    @Operation(summary = "Obtiene un producto y su stock", description = "Recupera los detalles de un producto específico junto con su cantidad disponible en inventario.")
    @ApiResponse(responseCode = "200", description = "Producto y stock encontrados")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    public Mono<ResponseEntity<JsonApiResponse<ProductoConStockAttributes>>> getProductoByIdWithStock(@PathVariable Long id) {
        return productoService.getProductoByIdWithStock(id)
                .map(dto -> {
                    ProductoConStockAttributes attributes = new ProductoConStockAttributes(
                            dto.getNombre(),
                            dto.getDescripcion(),
                            dto.getPrecio(),
                            dto.getStockDisponible()
                    );
                    JsonApiData<ProductoConStockAttributes> data = new JsonApiData<>(
                            dto.getId().toString(),
                            "productos-con-stock", // Un tipo distinto para este recurso combinado
                            attributes
                    );
                    return new ResponseEntity<>(new JsonApiResponse<>(data), HttpStatus.OK);
                })
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}

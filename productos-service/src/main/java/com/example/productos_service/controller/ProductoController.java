package com.example.productos_service.controller;

import com.example.productos_service.dto.ProductoAttributes;
import com.example.productos_service.dto.ProductoResource;
import com.example.productos_service.model.Producto;
import com.example.productos_service.service.ProductoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/productos", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // --- Endpoint para Crear un Producto (POST) ---
    // Recibe un ProductoResource en el cuerpo de la petición.
    // Retorna un 201 Created con el recurso del producto creado y la URL en la cabecera Location.
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE) // Espera JSON en la petición
    public ResponseEntity<ProductoResource> crearProducto(@RequestBody ProductoResource requestResource, UriComponentsBuilder ucb) {
        // Validar que el DTO de entrada no sea nulo y contenga atributos
        if (requestResource == null || requestResource.getData() == null || requestResource.getData().getAttributes() == null) {
            // En un caso real, aquí se podría lanzar una excepción de validación
            // o construir una respuesta de error JSON API más detallada.
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        ProductoAttributes attributes = requestResource.getData().getAttributes();
        String productId = requestResource.getData().getId(); // Intenta obtener el ID del request

        // Si el ID no viene en el request o está vacío, genera uno nuevo (UUID)
        if (productId == null || productId.isEmpty()) {
            productId = UUID.randomUUID().toString();
        }

        // Convertir DTO (ProductoAttributes) a Entidad (Producto)
        Producto productoParaGuardar = new Producto(productId, attributes.getNombre(), attributes.getPrecio());

        // Llamar al servicio para guardar el producto
        Producto nuevoProducto = productoService.crearProducto(productoParaGuardar);

        // Convertir la Entidad (Producto) a DTO (ProductoResource) para la respuesta JSON API
        ProductoResource responseResource = new ProductoResource();
        responseResource.setData(new ProductoResource.ProductData(
                nuevoProducto.getId(),
                "productos", // El 'type' para JSON API, típicamente el nombre del recurso en plural
                new ProductoAttributes(nuevoProducto.getNombre(), nuevoProducto.getPrecio())
        ));

        // Construir la URI del recurso recién creado para la cabecera 'Location' (JSON API best practice)
        URI locationUri = ucb.path("/productos/{id}")
                .buildAndExpand(nuevoProducto.getId())
                .toUri();

        return ResponseEntity.created(locationUri).body(responseResource); // Retorna 201 Created
    }

    // --- Endpoint para Obtener un Producto por ID (GET) ---
    // Retorna un 200 OK con el recurso del producto si es encontrado, o un 404 Not Found si no.
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResource> obtenerProductoPorId(@PathVariable String id) {
        // Llama al servicio para obtener el producto. El servicio maneja la ResourceNotFoundException.
        Producto producto = productoService.obtenerProducto(id);

        // Convertir la Entidad (Producto) a DTO (ProductoResource) para la respuesta JSON API
        ProductoResource responseResource = new ProductoResource();
        responseResource.setData(new ProductoResource.ProductData(
                producto.getId(),
                "productos",
                new ProductoAttributes(producto.getNombre(), producto.getPrecio())
        ));

        return ResponseEntity.ok(responseResource); // Retorna 200 OK
    }

    // --- Endpoint para Listar Productos con Paginación (GET) ---
    // Retorna un 200 OK con una lista de recursos de producto.
    @GetMapping
    public ResponseEntity<List<ProductoResource.ProductData>> listarProductos(
            @RequestParam(defaultValue = "0") int page, // Número de página (por defecto 0)
            @RequestParam(defaultValue = "10") int size) { // Tamaño de la página (por defecto 10)

        Pageable pageable = PageRequest.of(page, size); // Crea un objeto Pageable
        List<Producto> productos = productoService.listarProductos(pageable); // Llama al servicio

        // Mapear la lista de entidades Producto a una lista de ProductData (parte de JSON API)
        // Para una implementación completa de JSON API de colecciones, se necesitarían
        // 'links' y 'meta' para la paginación, dentro de un objeto raíz 'ProductoListResource'.
        // Aquí devolvemos directamente la lista de objetos 'data'.
        List<ProductoResource.ProductData> productDataList = productos.stream()
                .map(p -> new ProductoResource.ProductData(p.getId(), "productos", new ProductoAttributes(p.getNombre(), p.getPrecio())))
                .collect(Collectors.toList());

        return ResponseEntity.ok(productDataList); // Retorna 200 OK
    }

    // --- Endpoint para Actualizar un Producto (PUT) ---
    // Recibe un ProductoResource y el ID.
    // Retorna un 200 OK con el recurso actualizado, o 404 Not Found si el producto no existe.
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductoResource> actualizarProducto(@PathVariable String id, @RequestBody ProductoResource requestResource) {
        // Validar que el DTO de entrada no sea nulo y contenga atributos
        if (requestResource == null || requestResource.getData() == null || requestResource.getData().getAttributes() == null) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }

        ProductoAttributes attributes = requestResource.getData().getAttributes();

        // Convertir DTO a Entidad (Producto) para el servicio
        // Usamos el ID del path, no el del requestResource, para asegurar que actualizamos el recurso correcto.
        Producto productoParaActualizar = new Producto(id, attributes.getNombre(), attributes.getPrecio());

        // Llamar al servicio para actualizar el producto
        Producto updatedProducto = productoService.actualizarProducto(id, productoParaActualizar);

        // Convertir la Entidad (Producto) a DTO (ProductoResource) para la respuesta JSON API
        ProductoResource responseResource = new ProductoResource();
        responseResource.setData(new ProductoResource.ProductData(
                updatedProducto.getId(),
                "productos",
                new ProductoAttributes(updatedProducto.getNombre(), updatedProducto.getPrecio())
        ));

        return ResponseEntity.ok(responseResource); // Retorna 200 OK
    }

    // --- Endpoint para Eliminar un Producto (DELETE) ---
    // Retorna un 204 No Content si la eliminación es exitosa, o 404 Not Found si el producto no existe.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable String id) {
        productoService.eliminarProducto(id); // Llama al servicio
        return ResponseEntity.noContent().build(); // Retorna 204 No Content
    }
}

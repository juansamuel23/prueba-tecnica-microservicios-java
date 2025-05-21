package com.example.inventario_service.controller;

import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.inventario_service.jsonapi.JsonApiData;
import com.example.inventario_service.jsonapi.JsonApiResponse;
import com.example.inventario_service.jsonapi.InventarioAttributes;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {


    @Autowired
    private InventarioService inventarioService;

    /**
     * Crea o actualiza una entrada de inventario.
     * POST /api/inventario
     * @param inventario El objeto Inventario a guardar.
     * @return ResponseEntity con el objeto Inventario creado/actualizado y 201 CREATED.
     */
    @PostMapping
    @Operation(summary = "Crea o actualiza una entrada de inventario", description = "Añade una nueva entrada de inventario o actualiza la cantidad de un producto existente.")
    @ApiResponse(responseCode = "201", description = "Entrada de inventario creada/actualizada exitosamente")
    @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    public ResponseEntity<JsonApiResponse<InventarioAttributes>> createOrUpdateInventario(@RequestBody Inventario inventario) {
        Inventario savedInventario = inventarioService.saveInventario(inventario);

        // Convertir la entidad Inventario a JsonApiData<InventarioAttributes>
        InventarioAttributes attributes = new InventarioAttributes(
                savedInventario.getProductoId(),
                savedInventario.getCantidad()
        );
        JsonApiData<InventarioAttributes> data = new JsonApiData<>(
                savedInventario.getId().toString(), // El ID debe ser String en JSON API
                "inventarios", // Tipo de recurso
                attributes
        );

        JsonApiResponse<InventarioAttributes> response = new JsonApiResponse<>(data);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Obtiene todas las entradas de inventario.
     * GET /api/inventario
     * @return ResponseEntity con la lista de Inventarios y 200 OK.
     */
    @GetMapping
    @Operation(summary = "Lista todas las entradas de inventario", description = "Recupera una lista de todas las entradas de inventario.")
    @ApiResponse(responseCode = "200", description = "Lista de inventario recuperada")
    public ResponseEntity<JsonApiResponse<InventarioAttributes>> getAllInventario() {
        Iterable<Inventario> inventarios = inventarioService.getAllInventario();

        // Convertir la lista de entidades Inventario a una lista de JsonApiData<InventarioAttributes>
        List<JsonApiData<InventarioAttributes>> dataList = ((List<Inventario>) inventarios).stream() // Cast a List para usar stream
                .map(inventario -> new JsonApiData<>(
                        inventario.getId().toString(),
                        "inventarios",
                        new InventarioAttributes(inventario.getProductoId(), inventario.getCantidad())
                ))
                .collect(Collectors.toList());

        JsonApiResponse<InventarioAttributes> response = new JsonApiResponse<>(dataList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Obtiene una entrada de inventario por el ID del producto.
     * GET /api/inventario/{productoId}
     * @param productoId El ID del producto.
     * @return ResponseEntity con la entrada de inventario, o 404 NOT_FOUND.
     */
    @GetMapping("/{productoId}")
    @Operation(summary = "Obtiene una entrada de inventario por ID de producto", description = "Recupera la información de inventario para un producto específico.")
    @ApiResponse(responseCode = "200", description = "Entrada de inventario encontrada")
    @ApiResponse(responseCode = "404", description = "Producto no encontrado en inventario")
    public ResponseEntity<JsonApiResponse<InventarioAttributes>> getInventarioByProductoId(@PathVariable Long productoId) {
        Optional<Inventario> inventarioOptional = inventarioService.getInventarioByProductoId(productoId);

        return inventarioOptional.map(inventario -> {
            // Convertir la entidad Inventario a JsonApiData<InventarioAttributes>
            InventarioAttributes attributes = new InventarioAttributes(
                    inventario.getProductoId(),
                    inventario.getCantidad()
            );
            JsonApiData<InventarioAttributes> data = new JsonApiData<>(
                    inventario.getId().toString(), // El ID del registro de inventario (no el productoId)
                    "inventarios", // Tipo de recurso
                    attributes
            );
            return new ResponseEntity<>(new JsonApiResponse<>(data), HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Reduce la cantidad de stock de un producto dado su ID y la cantidad a reducir.
     * PUT /api/inventario/comprar/{productoId}/{cantidad}
     * @param productoId El ID del producto.
     * @param cantidad La cantidad a reducir.
     * @return ResponseEntity con la entrada de inventario actualizada, 400 BAD_REQUEST si no hay stock, o 404 NOT_FOUND.
     */
    @PutMapping("/comprar/{productoId}/{cantidad}")
    @Operation(summary = "Reduce el stock de un producto en inventario", description = "Decrementa la cantidad disponible de un producto en el inventario.")
    @ApiResponse(responseCode = "200", description = "Stock reducido exitosamente")
    @ApiResponse(responseCode = "400", description = "Stock insuficiente o producto no encontrado")
    public ResponseEntity<JsonApiResponse<InventarioAttributes>> reducirStockProducto(
            @PathVariable Long productoId,
            @PathVariable Integer cantidad) {
        try {
            Inventario updatedInventario = inventarioService.reduceStock(productoId, cantidad);

            // Convertir la entidad Inventario a JsonApiData<InventarioAttributes>
            InventarioAttributes attributes = new InventarioAttributes(
                    updatedInventario.getProductoId(),
                    updatedInventario.getCantidad()
            );
            JsonApiData<InventarioAttributes> data = new JsonApiData<>(
                    updatedInventario.getId().toString(),
                    "inventarios",
                    attributes
            );
            return new ResponseEntity<>(new JsonApiResponse<>(data), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Si el servicio de inventario lanza una IllegalArgumentException (ej. stock insuficiente)
            // Puedes devolver un 400 Bad Request
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}

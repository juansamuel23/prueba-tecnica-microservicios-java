package com.example.inventario_service.controller;

import com.example.inventario_service.dto.InventarioRequest;
import com.example.inventario_service.dto.InventarioResponse;
import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Inventario> createOrUpdateInventario(@RequestBody Inventario inventario) {
        Inventario savedInventario = inventarioService.saveInventario(inventario);
        return new ResponseEntity<>(savedInventario, HttpStatus.CREATED);
    }

    /**
     * Obtiene todas las entradas de inventario.
     * GET /api/inventario
     * @return ResponseEntity con la lista de Inventarios y 200 OK.
     */
    @GetMapping
    public ResponseEntity<Iterable<Inventario>> getAllInventario() {
        Iterable<Inventario> inventarios = inventarioService.getAllInventario();
        return new ResponseEntity<>(inventarios, HttpStatus.OK);
    }

    /**
     * Obtiene una entrada de inventario por el ID del producto.
     * GET /api/inventario/{productoId}
     * @param productoId El ID del producto.
     * @return ResponseEntity con la entrada de inventario, o 404 NOT_FOUND.
     */
    @GetMapping("/{productoId}")
    public ResponseEntity<Inventario> getInventarioByProductoId(@PathVariable Long productoId) {
        return inventarioService.getInventarioByProductoId(productoId)
                .map(inventario -> new ResponseEntity<>(inventario, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Reduce la cantidad de stock de un producto dado su ID y la cantidad a reducir.
     * PUT /api/inventario/comprar/{productoId}/{cantidad}
     * @param productoId El ID del producto.
     * @param cantidad La cantidad a reducir.
     * @return ResponseEntity con la entrada de inventario actualizada, 400 BAD_REQUEST si no hay stock, o 404 NOT_FOUND.
     */
    @PutMapping("/comprar/{productoId}/{cantidad}")
    public ResponseEntity<Inventario> reduceStock(@PathVariable Long productoId, @PathVariable Integer cantidad) {
        try {
            Inventario updatedInventario = inventarioService.reduceStock(productoId, cantidad);
            return new ResponseEntity<>(updatedInventario, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Captura errores como "No hay suficiente stock" o "Producto no encontrado"
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

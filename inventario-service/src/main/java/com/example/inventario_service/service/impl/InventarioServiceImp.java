package com.example.inventario_service.service.impl;


import com.example.inventario_service.dto.InventarioRequest;
import com.example.inventario_service.dto.InventarioResponse;
import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.repository.InventarioRepository;
import com.example.inventario_service.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventarioServiceImp implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    /**
     * Guarda o actualiza una entrada de inventario. Si ya existe un inventario para el productoId, lo actualiza.
     * @param inventario Objeto Inventario con productoId y cantidad.
     * @return El objeto Inventario guardado o actualizado.
     */
    @Override
    public Inventario saveInventario(Inventario inventario) {
        // Busca si ya existe una entrada de inventario para este productoId
        Optional<Inventario> existingInventario = inventarioRepository.findByProductoId(inventario.getProductoId());
        if (existingInventario.isPresent()) {
            // Si existe, actualiza su cantidad
            Inventario updatedInventario = existingInventario.get();
            updatedInventario.setCantidad(inventario.getCantidad());
            System.out.println("Actualizando inventario para producto " + inventario.getProductoId() + " a cantidad: " + inventario.getCantidad());
            return inventarioRepository.save(updatedInventario);
        } else {
            // Si no existe, crea una nueva entrada
            System.out.println("Creando inventario para producto " + inventario.getProductoId() + " con cantidad: " + inventario.getCantidad());
            return inventarioRepository.save(inventario);
        }
    }

    /**
     * Obtiene todas las entradas de inventario.
     * @return Iterable de objetos Inventario.
     */
    @Override
    public Iterable<Inventario> getAllInventario() {
        return inventarioRepository.findAll();
    }

    /**
     * Obtiene una entrada de inventario por el ID del producto.
     * @param productoId El ID del producto.
     * @return Optional<Inventario> que puede contener la entrada de inventario si se encuentra.
     */
    @Override
    public Optional<Inventario> getInventarioByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    /**
     * Reduce la cantidad de stock de un producto dado su ID y la cantidad a reducir.
     * @param productoId El ID del producto.
     * @param cantidad La cantidad a reducir.
     * @return El objeto Inventario actualizado.
     * @throws IllegalArgumentException Si no hay suficiente stock o el producto no se encuentra.
     */
    @Override
    public Inventario reduceStock(Long productoId, Integer cantidad) {
        Optional<Inventario> inventarioOptional = inventarioRepository.findByProductoId(productoId);
        if (inventarioOptional.isPresent()) {
            Inventario inventario = inventarioOptional.get();
            if (inventario.getCantidad() >= cantidad) {
                inventario.setCantidad(inventario.getCantidad() - cantidad);
                System.out.println("Reduciendo stock para producto " + productoId + " en " + cantidad + ". Nuevo stock: " + inventario.getCantidad());
                return inventarioRepository.save(inventario);
            } else {
                throw new IllegalArgumentException("No hay suficiente stock para el producto " + productoId + ". Stock actual: " + inventario.getCantidad() + ", Solicitado: " + cantidad);
            }
        } else {
            throw new IllegalArgumentException("Producto con ID " + productoId + " no encontrado en el inventario.");
        }
    }
}

package com.example.inventario_service.service;

import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.repository.InventarioRepository;
import com.example.inventario_service.service.impl.InventarioServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImp inventarioService;

    private Inventario inventario;

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProductoId(101L);
        inventario.setCantidad(100);
    }

    @Test
    void testSaveInventarioCreateNew() {
        when(inventarioRepository.findByProductoId(anyLong())).thenReturn(Optional.empty());
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventario);

        Inventario newInventario = new Inventario();
        newInventario.setProductoId(101L);
        newInventario.setCantidad(50);

        Inventario result = inventarioService.saveInventario(newInventario);

        assertNotNull(result);
        assertEquals(101L, result.getProductoId());
        assertEquals(100, result.getCantidad()); // Mocked response quantity
        verify(inventarioRepository, times(1)).findByProductoId(101L);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void testSaveInventarioUpdateExisting() {
        Inventario existingInventario = new Inventario();
        existingInventario.setId(1L);
        existingInventario.setProductoId(101L);
        existingInventario.setCantidad(100);

        when(inventarioRepository.findByProductoId(101L)).thenReturn(Optional.of(existingInventario));

        // Corrección: Usar thenAnswer para devolver el objeto Inventario que se pasa al save.
        // Esto simula que el repositorio guarda el objeto modificado por el servicio.
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario savedInventario = invocation.getArgument(0); // Obtiene el objeto que se intentó guardar
            return savedInventario; // Retorna el mismo objeto modificado
        });

        Inventario updateInventario = new Inventario();
        updateInventario.setProductoId(101L);
        updateInventario.setCantidad(120); // Nueva cantidad

        Inventario result = inventarioService.saveInventario(updateInventario);

        assertNotNull(result);
        assertEquals(101L, result.getProductoId());
        assertEquals(120, result.getCantidad()); // Corrección: Espera la cantidad actualizada (120)
        verify(inventarioRepository, times(1)).findByProductoId(101L);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    void testGetInventarioByProductoIdFound() {
        when(inventarioRepository.findByProductoId(101L)).thenReturn(Optional.of(inventario));

        Optional<Inventario> result = inventarioService.getInventarioByProductoId(101L);

        assertTrue(result.isPresent());
        assertEquals(100, result.get().getCantidad());
        verify(inventarioRepository, times(1)).findByProductoId(101L);
    }

    @Test
    void testGetInventarioByProductoIdNotFound() {
        when(inventarioRepository.findByProductoId(anyLong())).thenReturn(Optional.empty());

        Optional<Inventario> result = inventarioService.getInventarioByProductoId(999L);

        assertFalse(result.isPresent());
        verify(inventarioRepository, times(1)).findByProductoId(999L);
    }

    @Test
    void testGetAllInventario() {
        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventario, new Inventario()));

        Iterable<Inventario> result = inventarioService.getAllInventario();

        assertNotNull(result);
        assertTrue(result instanceof List<Inventario>);
        assertEquals(2, ((List<Inventario>)result).size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void testReducirStockSuccess() {
        Inventario existingInventario = new Inventario();
        existingInventario.setId(1L);
        existingInventario.setProductoId(101L);
        existingInventario.setCantidad(100);

        when(inventarioRepository.findByProductoId(101L)).thenReturn(Optional.of(existingInventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario invToSave = invocation.getArgument(0);
            return invToSave; // Return the modified inventory object
        });

        Inventario result = inventarioService.reduceStock(101L, 10);

        assertNotNull(result);
        assertEquals(90, result.getCantidad()); // 100 - 10 = 90
        verify(inventarioRepository, times(1)).findByProductoId(101L);
        verify(inventarioRepository, times(1)).save(existingInventario);
    }

    @Test
    void testReducirStockProductoNotFound() {
        when(inventarioRepository.findByProductoId(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.reduceStock(999L, 10);
        });
        // Corrección: El mensaje de la excepción debe coincidir exactamente con el del servicio.
        assertEquals("Producto con ID 999 no encontrado en el inventario.", exception.getMessage());
        verify(inventarioRepository, times(1)).findByProductoId(999L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    void testReducirStockInsufficientStock() {
        Inventario existingInventario = new Inventario();
        existingInventario.setId(1L);
        existingInventario.setProductoId(101L);
        existingInventario.setCantidad(5); // Only 5 in stock

        when(inventarioRepository.findByProductoId(101L)).thenReturn(Optional.of(existingInventario));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventarioService.reduceStock(101L, 10); // Try to reduce by 10
        });
        // Corrección: El mensaje de la excepción debe coincidir exactamente con el del servicio.
        assertEquals("No hay suficiente stock para el producto 101. Stock actual: 5, Solicitado: 10", exception.getMessage());
        verify(inventarioRepository, times(1)).findByProductoId(101L);
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}

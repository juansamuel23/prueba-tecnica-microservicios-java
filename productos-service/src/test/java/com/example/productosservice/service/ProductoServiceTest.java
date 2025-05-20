package com.example.productosservice.service;

import com.example.productos_service.exception.ResourceNotFoundException;
import com.example.productos_service.model.Producto;
import com.example.productos_service.repository.ProductoRepository;
import com.example.productos_service.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class) // Habilita Mockito con JUnit 5
public class ProductoServiceTest {
    @Mock // Crea un mock del ProductoRepository
    private ProductoRepository productoRepository;

    @InjectMocks // Inyecta los mocks en la instancia de ProductoServiceImpl
    private ProductoServiceImpl productoService; // La implementación del servicio

    private Producto testProducto;

    @BeforeEach
        // Se ejecuta antes de cada test
    void setUp() {
        // Inicializa un producto de prueba para usar en los tests
        testProducto = new Producto(UUID.randomUUID().toString(), "Laptop Gaming", new BigDecimal("1500.00"));
    }

    @Test
    void crearProducto_deberiaGuardarYRetornarProducto() {
        // Dado (Given)
        when(productoRepository.save(any(Producto.class))).thenReturn(testProducto);

        // Cuando (When)
        Producto resultado = productoService.crearProducto(testProducto);

        // Entonces (Then)
        assertNotNull(resultado);
        assertEquals(testProducto.getId(), resultado.getId());
        assertEquals(testProducto.getNombre(), resultado.getNombre());
        assertEquals(testProducto.getPrecio(), resultado.getPrecio());
        verify(productoRepository, times(1)).save(any(Producto.class)); // Verifica que save fue llamado una vez
    }

    @Test
    void obtenerProducto_deberiaRetornarProductoSiExiste() {
        // Dado
        when(productoRepository.findById(testProducto.getId())).thenReturn(Optional.of(testProducto));

        // Cuando
        Producto resultado = productoService.obtenerProducto(testProducto.getId());

        // Entonces
        assertNotNull(resultado);
        assertEquals(testProducto.getId(), resultado.getId());
        verify(productoRepository, times(1)).findById(testProducto.getId());
    }

    @Test
    void obtenerProducto_deberiaLanzarExcepcionSiNoExiste() {
        // Dado
        when(productoRepository.findById("nonExistentId")).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerProducto("nonExistentId"));
        verify(productoRepository, times(1)).findById("nonExistentId");
    }

    @Test
    void listarProductos_deberiaRetornarListaDeProductos() {
        // Dado
        List<Producto> productos = Arrays.asList(testProducto,
                new Producto(UUID.randomUUID().toString(), "Teclado", new BigDecimal("100.00")));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Producto> page = new PageImpl<>(productos, pageable, productos.size());

        when(productoRepository.findAll(pageable)).thenReturn(page);

        // Cuando
        List<Producto> resultado = productoService.listarProductos(pageable);

        // Entonces
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(testProducto));
        verify(productoRepository, times(1)).findAll(pageable);
    }

    @Test
    void actualizarProducto_deberiaActualizarYRetornarProducto() {
        // Dado
        Producto productoActualizado = new Producto(testProducto.getId(), "Laptop Ultra", new BigDecimal("1800.00"));
        when(productoRepository.findById(testProducto.getId())).thenReturn(Optional.of(testProducto));
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActualizado);

        // Cuando
        Producto resultado = productoService.actualizarProducto(testProducto.getId(), productoActualizado);

        // Entonces
        assertNotNull(resultado);
        assertEquals("Laptop Ultra", resultado.getNombre());
        assertEquals(new BigDecimal("1800.00"), resultado.getPrecio());
        verify(productoRepository, times(1)).findById(testProducto.getId());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    void actualizarProducto_deberiaLanzarExcepcionSiNoExiste() {
        // Dado
        Producto productoActualizado = new Producto("nonExistentId", "Laptop Ultra", new BigDecimal("1800.00"));
        when(productoRepository.findById("nonExistentId")).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(ResourceNotFoundException.class, () -> productoService.actualizarProducto("nonExistentId", productoActualizado));
        verify(productoRepository, times(1)).findById("nonExistentId");
        verify(productoRepository, never()).save(any(Producto.class)); // Asegura que save nunca fue llamado
    }

    @Test
    void eliminarProducto_deberiaEliminarProductoExistente() {
        // Dado
        when(productoRepository.findById(testProducto.getId())).thenReturn(Optional.of(testProducto));
        doNothing().when(productoRepository).delete(any(Producto.class)); // Para métodos void

        // Cuando
        productoService.eliminarProducto(testProducto.getId());

        // Entonces
        verify(productoRepository, times(1)).findById(testProducto.getId());
        verify(productoRepository, times(1)).delete(testProducto); // Verifica que delete fue llamado
    }

    @Test
    void eliminarProducto_deberiaLanzarExcepcionSiNoExiste() {
        // Dado
        when(productoRepository.findById("nonExistentId")).thenReturn(Optional.empty());

        // Cuando / Entonces
        assertThrows(ResourceNotFoundException.class, () -> productoService.eliminarProducto("nonExistentId"));
        verify(productoRepository, times(1)).findById("nonExistentId");
        verify(productoRepository, never()).delete(any(Producto.class)); // Asegura que delete nunca fue llamado
    }
}

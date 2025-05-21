package com.example.productosservice.service;

import com.example.productos_service.client.InventarioServiceClient;
import com.example.productos_service.client.model.InventarioResponse;
import com.example.productos_service.exception.ResourceNotFoundException;
import com.example.productos_service.model.Producto;
import com.example.productos_service.model.ProductoConStockDTO;
import com.example.productos_service.repository.ProductoRepository;
import com.example.productos_service.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier; // Para probar Mono

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioServiceClient inventarioServiceClient;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto producto;
    private Producto otroProducto;

    @BeforeEach
    void setUp() {
        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Laptop");
        producto.setDescripcion("Una laptop potente");
        producto.setPrecio(BigDecimal.valueOf(1200.00));

        otroProducto = new Producto();
        otroProducto.setId(2L);
        otroProducto.setNombre("Monitor");
        otroProducto.setDescripcion("Monitor 4K");
        otroProducto.setPrecio(BigDecimal.valueOf(400.00));
    }

    @Test
    void testSaveProducto() {
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        InventarioResponse mockInventarioResponse = new InventarioResponse();
        mockInventarioResponse.setProductoId(producto.getId());
        mockInventarioResponse.setCantidad(0);
        when(inventarioServiceClient.crearInventario(anyLong(), anyInt()))
                .thenReturn(Mono.just(mockInventarioResponse));

        Producto result = productoService.saveProducto(producto);

        assertNotNull(result);
        assertEquals("Laptop", result.getNombre());
        verify(productoRepository, times(1)).save(producto);

        ArgumentCaptor<Long> productoIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> cantidadCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(inventarioServiceClient, times(1)).crearInventario(productoIdCaptor.capture(), cantidadCaptor.capture());

        assertEquals(producto.getId(), productoIdCaptor.getValue());
        assertEquals(0, cantidadCaptor.getValue());
    }

    @Test
    void testGetProductoByIdWithStockFound() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        InventarioResponse inventarioResponse = new InventarioResponse();
        inventarioResponse.setProductoId(1L);
        inventarioResponse.setCantidad(10);
        when(inventarioServiceClient.obtenerInventarioPorProductoId(1L))
                .thenReturn(Mono.just(inventarioResponse));

        Mono<ProductoConStockDTO> resultMono = productoService.getProductoByIdWithStock(1L);

        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getNombre().equals("Laptop") &&
                                dto.getStockDisponible().equals(10)
                )
                .verifyComplete();

        verify(productoRepository, times(1)).findById(1L);
        verify(inventarioServiceClient, times(1)).obtenerInventarioPorProductoId(1L);
    }

    @Test
    void testGetProductoByIdWithStockNotFoundProducto() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Mono<ProductoConStockDTO> resultMono = productoService.getProductoByIdWithStock(99L);

        StepVerifier.create(resultMono)
                .verifyComplete();

        verify(productoRepository, times(1)).findById(99L);
        verifyNoInteractions(inventarioServiceClient);
    }

    @Test
    void testGetProductoByIdWithStockInventarioNotFound() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        when(inventarioServiceClient.obtenerInventarioPorProductoId(1L))
                .thenReturn(Mono.empty());

        Mono<ProductoConStockDTO> resultMono = productoService.getProductoByIdWithStock(1L);

        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getNombre().equals("Laptop") &&
                                dto.getStockDisponible().equals(0)
                )
                .verifyComplete();

        verify(productoRepository, times(1)).findById(1L);
        verify(inventarioServiceClient, times(1)).obtenerInventarioPorProductoId(1L);
    }

    @Test
    void testGetProductoByIdWithStockInventarioServiceError() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        when(inventarioServiceClient.obtenerInventarioPorProductoId(1L))
                .thenReturn(Mono.error(new RuntimeException("Error de comunicación con inventario")));

        Mono<ProductoConStockDTO> resultMono = productoService.getProductoByIdWithStock(1L);

        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getNombre().equals("Laptop") &&
                                dto.getStockDisponible().equals(0)
                )
                .verifyComplete();

        verify(productoRepository, times(1)).findById(1L);
        verify(inventarioServiceClient, times(1)).obtenerInventarioPorProductoId(1L);
    }

    @Test
    void testReducirStockProductoSuccess() {
        // 1. Mockear la llamada a inventarioServiceClient.reducirStock
        // Simula que la reducción fue exitosa y devuelve un inventario actualizado.
        InventarioResponse reducedInventarioResponse = new InventarioResponse();
        reducedInventarioResponse.setProductoId(1L);
        reducedInventarioResponse.setCantidad(15); // Stock después de la reducción
        when(inventarioServiceClient.reducirStock(1L, 5))
                .thenReturn(Mono.just(reducedInventarioResponse));

        // 2. Mockear las DEPENDENCIAS de getProductoByIdWithStock, ya que es llamado internamente.
        // Mockear productoRepository.findById para que getProductoByIdWithStock encuentre el producto.
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        // Mockear inventarioServiceClient.obtenerInventarioPorProductoId
        // para que getProductoByIdWithStock obtenga el stock actualizado.
        InventarioResponse currentInventarioAfterReduction = new InventarioResponse();
        currentInventarioAfterReduction.setProductoId(1L);
        currentInventarioAfterReduction.setCantidad(15); // El stock que getProductoByIdWithStock debería ver
        when(inventarioServiceClient.obtenerInventarioPorProductoId(1L))
                .thenReturn(Mono.just(currentInventarioAfterReduction));

        // Ejecutar el método del servicio
        Mono<ProductoConStockDTO> resultMono = productoService.reducirStockProducto(1L, 5);

        // Verificar el resultado con StepVerifier
        StepVerifier.create(resultMono)
                .expectNextMatches(dto ->
                        dto.getId().equals(1L) &&
                                dto.getNombre().equals("Laptop") &&
                                dto.getStockDisponible().equals(15) // Esperamos el stock actualizado
                )
                .verifyComplete();

        // Verificar interacciones con los mocks
        verify(inventarioServiceClient, times(1)).reducirStock(1L, 5);
        verify(productoRepository, times(1)).findById(1L); // Llamada de getProductoByIdWithStock
        verify(inventarioServiceClient, times(1)).obtenerInventarioPorProductoId(1L); // Llamada de getProductoByIdWithStock
    }

    @Test
    void testReducirStockProductoThrowsExceptionWhenStockInsufficient() {
        when(inventarioServiceClient.reducirStock(1L, 10))
                .thenReturn(Mono.error(new IllegalArgumentException("Stock insuficiente para el productoId: 1. Cantidad disponible: 5, Cantidad a reducir: 10")));

        Mono<ProductoConStockDTO> resultMono = productoService.reducirStockProducto(1L, 10);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().contains("Stock insuficiente"))
                .verify();

        verify(inventarioServiceClient, times(1)).reducirStock(1L, 10);
        // Asegurarse de que las dependencias de getProductoByIdWithStock NO se llaman si reducirStock falla
        verify(productoRepository, never()).findById(anyLong());
        verify(inventarioServiceClient, never()).obtenerInventarioPorProductoId(anyLong());
    }

    @Test
    void testReducirStockProductoNotFoundOrInventarioProblem() {
        when(inventarioServiceClient.reducirStock(99L, 5))
                .thenReturn(Mono.empty());

        Mono<ProductoConStockDTO> resultMono = productoService.reducirStockProducto(99L, 5);

        StepVerifier.create(resultMono)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().contains("Producto o inventario no encontrado durante la reducción de stock."))
                .verify();

        verify(inventarioServiceClient, times(1)).reducirStock(99L, 5);
        // Asegurarse de que las dependencias de getProductoByIdWithStock NO se llaman
        verify(productoRepository, never()).findById(anyLong());
        verify(inventarioServiceClient, never()).obtenerInventarioPorProductoId(anyLong());
    }

    // --- Métodos de CRUD que no interactúan con InventarioServiceClient ---

    @Test
    void testGetProductoById() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> result = productoService.getProductoById(1L);

        assertTrue(result.isPresent());
        assertEquals("Laptop", result.get().getNombre());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductoByIdNotFound() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Producto> result = productoService.getProductoById(1L);

        assertFalse(result.isPresent());
        verify(productoRepository, times(1)).findById(1L);
    }

    // Los tests para updateProducto han sido eliminados ya que el método no existe en el servicio provisto.
    // Si lo implementas en tu ProductoServiceImpl, deberás añadir los tests aquí.

    @Test
    void testDeleteProductoSuccess() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        doNothing().when(productoRepository).deleteById(1L);

        assertDoesNotThrow(() -> productoService.deleteProducto(1L));
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteProductoNotFound() {
        when(productoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.deleteProducto(1L));
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, never()).deleteById(anyLong());
    }

    @Test
    void testGetAllProductos() {
        List<Producto> productos = Arrays.asList(producto, otroProducto);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Producto> page = new PageImpl<>(productos, pageable, productos.size());

        when(productoRepository.findAll(pageable)).thenReturn(page);

        Page<Producto> result = productoService.getAllProductos(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getNombre());
        verify(productoRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAllProductosEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Producto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productoRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<Producto> result = productoService.getAllProductos(pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productoRepository, times(1)).findAll(pageable);
    }
}

package com.example.productosservice.controller;

import com.example.productos_service.ProductosServiceApplication;
import com.example.productos_service.client.InventarioServiceClient;
import com.example.productos_service.model.Producto;
import com.example.productos_service.repository.ProductoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;


import java.math.BigDecimal;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = ProductosServiceApplication.class)
@AutoConfigureMockMvc // Para usar MockMvc
@Testcontainers // Habilita Testcontainers
public class ProductoControllerIT {

    @Container // Esto levantará un contenedor PostgreSQL para la prueba
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13.21")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresContainer::getPassword);
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); // Crea la tabla para cada test
        dynamicPropertyRegistry.add("spring.flyway.enabled", () -> "false"); // Deshabilita Flyway para los tests de integración
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @MockBean // Mockeamos el cliente de inventario para que la prueba se enfoque en el servicio de productos y su DB
    private InventarioServiceClient inventarioServiceClient;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll(); // Limpiar la DB antes de cada test
    }

    @Test
    void createProducto_shouldReturnCreatedProductInJsonApiFormat() throws Exception {
        Producto newProducto = new Producto();
        newProducto.setNombre("Nuevo Producto IT");
        newProducto.setDescripcion("Descripción del producto IT");
        newProducto.setPrecio(BigDecimal.valueOf(250.00));

        // Mockear el cliente de inventario ya que es una dependencia externa
        when(inventarioServiceClient.crearInventario(anyLong(), anyInt()))
                .thenReturn(Mono.empty());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type", is("productos")))
                .andExpect(jsonPath("$.data.attributes.nombre", is("Nuevo Producto IT")))
                .andExpect(jsonPath("$.data.attributes.precio", is(250.00)));

        // Verificar que el producto fue guardado en la DB
        assertEquals(1, productoRepository.count());
        Optional<Producto> savedProducto = productoRepository.findByNombre("Nuevo Producto IT");
        assertTrue(savedProducto.isPresent());
    }

    @Test
    void getProductoById_shouldReturnProductInJsonApiFormat() throws Exception {
        Producto existingProducto = new Producto();
        existingProducto.setNombre("Producto Existente");
        existingProducto.setDescripcion("Para test GET");
        existingProducto.setPrecio(BigDecimal.valueOf(99.99));
        Producto saved = productoRepository.save(existingProducto);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/productos/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(saved.getId().toString())))
                .andExpect(jsonPath("$.data.type", is("productos")))
                .andExpect(jsonPath("$.data.attributes.nombre", is("Producto Existente")));
    }


}

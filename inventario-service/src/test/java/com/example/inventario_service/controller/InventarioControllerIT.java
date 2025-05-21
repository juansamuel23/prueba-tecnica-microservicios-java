package com.example.inventario_service.controller;

import com.example.inventario_service.InventarioServiceApplication;
import com.example.inventario_service.model.Inventario;
import com.example.inventario_service.repository.InventarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = InventarioServiceApplication.class)
@AutoConfigureMockMvc
@Testcontainers
public class InventarioControllerIT {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13.21")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        dynamicPropertyRegistry.add("spring.datasource.username", postgresContainer::getUsername);
        dynamicPropertyRegistry.add("spring.datasource.password", postgresContainer::getPassword);
        dynamicPropertyRegistry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        dynamicPropertyRegistry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        inventarioRepository.deleteAll(); // Limpiar la DB antes de cada test
    }

    @Test
    void createOrUpdateInventario_shouldCreateNewInventarioEntry() throws Exception {
        Inventario newInventario = new Inventario();
        newInventario.setProductoId(1001L);
        newInventario.setCantidad(50);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newInventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type", is("inventarios")))
                .andExpect(jsonPath("$.data.attributes.productoId", is(1001)))
                .andExpect(jsonPath("$.data.attributes.cantidad", is(50)));

        assertEquals(1, inventarioRepository.count());
        assertTrue(inventarioRepository.findByProductoId(1001L).isPresent());
    }

    @Test
    void reducirStockProducto_shouldDecreaseStockSuccessfully() throws Exception {
        Inventario existingInventario = new Inventario();
        existingInventario.setProductoId(1002L);
        existingInventario.setCantidad(20);
        inventarioRepository.save(existingInventario);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventario/comprar/{productoId}/{cantidad}", 1002L, 5)
                        .contentType(MediaType.APPLICATION_JSON)) // Aunque es PUT, algunos métodos requieren content type
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.type", is("inventarios")))
                .andExpect(jsonPath("$.data.attributes.productoId", is(1002)))
                .andExpect(jsonPath("$.data.attributes.cantidad", is(15))); // 20 - 5 = 15

        Inventario updated = inventarioRepository.findByProductoId(1002L).get();
        assertEquals(15, updated.getCantidad());
    }

    @Test
    void reducirStockProducto_shouldReturnBadRequestForInsufficientStock() throws Exception {
        Inventario existingInventario = new Inventario();
        existingInventario.setProductoId(1003L);
        existingInventario.setCantidad(5);
        inventarioRepository.save(existingInventario);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/inventario/comprar/{productoId}/{cantidad}", 1003L, 10)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Stock no debería haber cambiado
        Inventario updated = inventarioRepository.findByProductoId(1003L).get();
        assertEquals(5, updated.getCantidad());
    }


}

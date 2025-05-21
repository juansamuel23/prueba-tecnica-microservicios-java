package com.example.productos_service.client;

import com.example.productos_service.client.model.InventarioResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class InventarioServiceClient {

    private final WebClient webClient;

    // Inyecta la URL base del servicio de inventario desde application.yml
    public InventarioServiceClient(@Value("${inventario-service.url}") String inventarioServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(inventarioServiceUrl).build();
    }

    /**
     * Crea o actualiza una entrada de inventario para un producto.
     * Corresponde al POST /api/inventario en el servicio de Inventario.
     * @param productoId El ID del producto.
     * @param cantidadInicial La cantidad de stock inicial.
     * @return Mono<InventarioResponse> con la entrada de inventario creada/actualizada.
     */
    public Mono<InventarioResponse> crearInventario(Long productoId, Integer cantidadInicial) {
        InventarioResponse inventarioRequest = new InventarioResponse();
        inventarioRequest.setProductoId(productoId);
        inventarioRequest.setCantidad(cantidadInicial);

        return webClient.post()
                .bodyValue(inventarioRequest)
                .retrieve()
                // Manejo de errores HTTP: si el estado es 4xx o 5xx, se propaga una excepción
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class) // Obtiene el cuerpo del error
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error al crear inventario (" + response.statusCode() + "): " + errorBody))))
                .bodyToMono(InventarioResponse.class);
    }

    /**
     * Obtiene la información de inventario para un producto específico.
     * Corresponde al GET /api/inventario/{productoId} en el servicio de Inventario.
     * @param productoId El ID del producto a buscar.
     * @return Mono<InventarioResponse> con el inventario del producto, o Mono.empty() si no se encuentra (404).
     */
    public Mono<InventarioResponse> obtenerInventarioPorProductoId(Long productoId) {
        return webClient.get()
                .uri("/{productoId}", productoId)
                .retrieve()
                // Manejo específico para 404 (Not Found): devuelve un Mono vacío
                .onStatus(status -> status.equals(org.springframework.http.HttpStatus.NOT_FOUND),
                        response -> Mono.empty())
                // Manejo de otros errores (4xx, 5xx)
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error al obtener inventario (" + response.statusCode() + "): " + errorBody))))
                .bodyToMono(InventarioResponse.class);
    }

    /**
     * Reduce la cantidad de stock de un producto.
     * Corresponde al PUT /api/inventario/comprar/{productoId}/{cantidad} en el servicio de Inventario.
     * @param productoId El ID del producto cuyo stock se va a reducir.
     * @param cantidadAReducir La cantidad a restar.
     * @return Mono<InventarioResponse> con la entrada de inventario actualizada.
     */
    public Mono<InventarioResponse> reducirStock(Long productoId, Integer cantidadAReducir) {
        return webClient.put()
                .uri("/comprar/{productoId}/{cantidad}", productoId, cantidadAReducir)
                .retrieve()
                // Manejo de errores HTTP
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error al reducir stock (" + response.statusCode() + "): " + errorBody))))
                .bodyToMono(InventarioResponse.class);
    }

    /**
     * Obtiene todas las entradas de inventario.
     * Corresponde al GET /api/inventario en el servicio de Inventario.
     * @return Mono<InventarioResponse[]> con un array de todas las entradas de inventario.
     */
    public Mono<InventarioResponse[]> obtenerTodosLosInventarios() {
        return webClient.get()
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new RuntimeException("Error al obtener todos los inventarios (" + response.statusCode() + "): " + errorBody))))
                .bodyToMono(InventarioResponse[].class);
    }
}

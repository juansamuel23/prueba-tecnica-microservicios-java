package com.example.inventario_service.client;


import com.example.inventario_service.model.ProductoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ProductoServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceClient.class);
    private final WebClient webClient;
    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${app.security.api-key}")
    private String apiKey; // La API Key que enviaremos a productos-service

    public ProductoServiceClient(WebClient.Builder webClientBuilder, @Value("${productos.service.url}") String productosServiceUrl) {
        // Configuramos el WebClient para incluir la cabecera de la API Key por defecto
        this.webClient = webClientBuilder
                .baseUrl(productosServiceUrl)
                // Usar defaultHeader para que se añada a todas las peticiones de este WebClient
                // Nota: Si solo quieres añadirla a llamadas específicas, hazlo en cada método (como en getProductoById)
                //.defaultHeader(API_KEY_HEADER, apiKey) // Esta línea se puede añadir si la API Key es la misma para todas las llamadas
                .build();
    }

    public Mono<ProductoResponse> getProductoById(Long productoId) {
        return webClient.get()
                .uri("/productos/{id}", productoId)
                .header(API_KEY_HEADER, apiKey) // Añade la cabecera de la API Key aquí
                .retrieve()
                .bodyToMono(ProductoResponse.class)
                .timeout(Duration.ofSeconds(3)) // Timeout de 3 segundos
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)) // Reintentos: 3 veces, con 2s de delay inicial exponencial
                        .jitter(0.5)
                        .filter(e -> e instanceof java.util.concurrent.TimeoutException || e instanceof org.springframework.web.reactive.function.client.WebClientRequestException)
                        .doBeforeRetry(retrySignal -> logger.warn("Reintentando llamada a productos-service para productoId {}. Intento: {}", productoId, (retrySignal.totalRetriesInARow() + 1)))
                )
                .doOnError(e -> logger.error("Error final al obtener producto {} desde el servicio de productos después de reintentos: {}", productoId, e.getMessage()))
                .onErrorResume(e -> {
                    // Si el error es de autenticación, o cualquier otro que no se manejó con retry, podemos manejarlo aquí
                    return Mono.empty(); // O lanzar una excepción específica
                });
    }
}

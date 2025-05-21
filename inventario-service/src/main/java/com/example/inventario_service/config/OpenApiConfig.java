package com.example.inventario_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Inventario Service API",
                version = "1.0",
                description = "API para la gestión del inventario de productos.",
                contact = @Contact(name = "Juan Pablo Torres Bermudez", email = "juansamuel230513@gmail.com"),
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        )
)
public class OpenApiConfig {
}

# Microservicios de Gestión de Productos e Inventario

Este proyecto implementa dos microservicios (Productos e Inventario) utilizando Spring Boot, Docker para containerización y Docker Compose para la orquestación. Incluye bases de datos PostgreSQL para cada servicio y una capa de seguridad básica mediante API Key.

## Contenido del Proyecto

* `productos-service/`: Microservicio para la gestión de productos.
* `inventario-service/`: Microservicio para la gestión de inventario.
* `docker-compose.yml`: Archivo de orquestación para levantar todos los servicios.
* `README.md`: Este archivo.

## Requisitos Previos

Antes de ejecutar el proyecto, asegúrate de tener instaladas las siguientes herramientas:

* **Java Development Kit (JDK) 21:** [Descargar JDK](https://www.oracle.com/java/technologies/downloads/)
* **Apache Maven:** [Descargar Maven](https://maven.apache.org/download.cgi)
* **Docker Desktop:** (Incluye Docker Engine y Docker Compose) [Descargar Docker Desktop](https://www.docker.com/products/docker-desktop/)
* **Postman (opcional):** Para probar las APIs. [Descargar Postman](https://www.postman.com/downloads/)

## Arquitectura y Principios de Diseño

Este proyecto ha sido diseñado siguiendo una arquitectura de microservicios, lo que permite el desarrollo, despliegue y escalado independiente de cada componente funcional.

### Arquitectura de Microservicios

* **Modularidad:** El sistema está dividido en dos servicios independientes: `productos-service` (para la gestión de productos) e `inventario-service` (para la gestión de stock). Cada uno es una unidad autónoma con su propia lógica de negocio y base de datos.
* **Comunicación Interna:** Los servicios se comunican entre sí a través de llamadas HTTP síncronas. Por ejemplo, `inventario-service` valida la existencia de un producto consultando a `productos-service` antes de agregar o actualizar el inventario.
* **Aislamiento de Datos:** Cada microservicio posee su propia base de datos (PostgreSQL), lo que garantiza la autonomía y reduce el acoplamiento entre servicios.
* **Despliegue Independiente:** Utilizando Docker y Docker Compose, cada microservicio puede ser construido y desplegado de forma individual, facilitando las actualizaciones y el mantenimiento.

### Principios de Diseño (SOLID)

Se han aplicado principios de diseño de software para asegurar la mantenibilidad, escalabilidad y robustez del código:

* **S (Single Responsibility Principle - Principio de Responsabilidad Única):** Cada clase y componente está diseñado para tener una única responsabilidad bien definida. Por ejemplo, las clases de servicio (`ProductoService`, `InventarioService`) se encargan de la lógica de negocio, mientras que los repositorios (`ProductoRepository`, `InventarioRepository`) se encargan de la interacción con la base de datos.
* **O (Open/Closed Principle - Principio Abierto/Cerrado):** El diseño busca que las entidades de software (clases, módulos, funciones, etc.) estén abiertas para la extensión, pero cerradas para la modificación. Esto se logra, por ejemplo, mediante el uso de interfaces y la inyección de dependencias en Spring.
* **L (Liskov Substitution Principle - Principio de Sustitución de Liskov):** Asegura que los objetos de una superclase puedan ser reemplazados por objetos de una subclase sin alterar la corrección del programa. Aunque no hay herencia compleja visible directamente en los servicios base, este principio guía el diseño de interfaces y abstracciones.
* **I (Interface Segregation Principle - Principio de Segregación de Interfaces):** Se promueve el uso de interfaces pequeñas y específicas en lugar de interfaces grandes y monolíticas. Esto evita que los clientes dependan de métodos que no utilizan. (Podrías mencionar si aplicaste interfaces para tus servicios o clientes Feign, por ejemplo).
* **D (Dependency Inversion Principle - Principio de Inversión de Dependencias):** Se busca que las dependencias entre módulos de alto nivel y bajo nivel se realicen a través de abstracciones (interfaces), no de implementaciones concretas. Spring Boot, con su inversión de control (IoC) y la inyección de dependencias, facilita enormemente la aplicación de este principio. Por ejemplo, los controladores dependen de interfaces de servicio, no de implementaciones concretas.

### Seguridad

* **API Key:** Se ha implementado una capa de seguridad básica utilizando un API Key (`X-API-Key`) que es validada en un filtro de Servlet (`ApiKeyAuthFilter`). Esta clave es necesaria para acceder a los endpoints protegidos, garantizando que solo los clientes autorizados puedan interactuar con la API.

## Ejecución del Proyecto

Sigue estos pasos para levantar y ejecutar todos los microservicios:

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/juansamuel23/prueba-tecnica-microservicios-java.git
    cd <nombre-de-tu-carpeta-de-proyecto>
    ```

2.  **Compilar los JARs de los Microservicios:**
    Este proyecto utiliza Dockerfiles de una sola etapa que copian los JARs ya construidos. Por lo tanto, es **crucial** compilar cada microservicio localmente antes de construir las imágenes Docker.

    ```bash
    cd productos-service
    mvn clean install -DskipTests
    cd ..
    cd inventario-service
    mvn clean install -DskipTests
    cd ..
    ```
    *Asegúrate de que los JARs se generen correctamente en la carpeta `target/` de cada servicio. Por defecto, los Dockerfiles esperan `target/nombre-del-servicio-0.0.1-SNAPSHOT.jar`.*

3.  **Levantar los Servicios con Docker Compose:**
    Navega a la raíz del proyecto (donde se encuentra `docker-compose.yml`) y ejecuta:
    ```bash
    docker-compose up --build -d
    ```
    * `--build`: Fuerza la reconstrucción de las imágenes Docker (necesario después de compilar los JARs o de cualquier cambio en el `Dockerfile`).
    * `-d`: Ejecuta los contenedores en modo 'detached' (segundo plano).

4.  **Verificar el Estado de los Contenedores:**
    Puedes verificar que todos los servicios estén corriendo correctamente:
    ```bash
    docker ps
    ```

5.  **Verificar los Logs (Opcional):**
    Para ver los logs de todos los servicios en tiempo real:
    ```bash
    docker-compose logs -f
    ```
    Presiona `Ctrl+C` para salir del seguimiento de logs.

## Pruebas de API con Postman

Las APIs están protegidas con una API Key. Sigue estos pasos para configurar Postman y probar los servicios:

### 1. Configurar la API Key en Postman

* **Valor de la API Key:** `SuperSecretaAPIKeyParaMicroservicios2025!`
* **En Postman, crea una variable de entorno:**
    * Haz clic en el icono "Environment" (ojo) > "Add".
    * Nombra el entorno (ej. "Microservicios Env").
    * Agrega una variable: `Key: apiKey`, `Initial Value: SuperSecretaAPIKeyParaMicroservicios2025!`, `Current Value: SuperSecretaAPIKeyParaMicroservicios2025!`.
    * Asegúrate de seleccionar este entorno.
* **En cada solicitud, ve a la pestaña `Headers` y agrega:**
    * `Key: X-API-Key`
    * `Value: {{apiKey}}` (Esto usará la variable de entorno).

### 2. Endpoints y Flujo de Prueba

A continuación, se describen los endpoints y un flujo de prueba sugerido:

**Microservicio de Productos (`http://localhost:8081`)**

* **Crear Producto (POST)**
    * **URL:** `http://localhost:8081/api/productos`
    * **Headers:** `Content-Type: application/json`, `X-API-Key: {{apiKey}}`
    * **Body (JSON):**
        ```json
        {
            "nombre": "Laptop Gamer XYZ",
            "descripcion": "Potente laptop para juegos de última generación",
            "precio": 1500.00
        }
        ```
    * *Guardar el `id` del producto creado para usarlo en el servicio de Inventario.*

* **Obtener Producto por ID (GET)**
    * **URL:** `http://localhost:8081/api/productos/{id_del_producto}`
    * **Headers:** `X-API-Key: {{apiKey}}`

* **Obtener Todos los Productos (GET)**
    * **URL:** `http://localhost:8081/api/productos`
    * **Headers:** `X-API-Key: {{apiKey}}`

**Microservicio de Inventario (`http://localhost:8082`)**

* **Agregar Inventario (POST)**
    * **URL:** `http://localhost:8082/api/inventario`
    * **Headers:** `Content-Type: application/json`, `X-API-Key: {{apiKey}}`
    * **Body (JSON):**
        ```json
        {
            "productoId": {id_del_producto_creado},
            "cantidad": 100,
            "ubicacion": "Almacén Principal A1"
        }
        ```
    * *El `productoId` debe ser el ID del producto creado anteriormente.*

* **Obtener Inventario por ID de Producto (GET)**
    * **URL:** `http://localhost:8082/api/inventario/producto/{id_del_producto}`
    * **Headers:** `X-API-Key: {{apiKey}}`

* **Obtener Todo el Inventario (GET)**
    * **URL:** `http://localhost:8082/api/inventario`
    * **Headers:** `X-API-Key: {{apiKey}}`

## Consideraciones Adicionales

* Los Dockerfiles utilizan una estrategia de una sola etapa (single-stage) para simplificar el proceso de construcción y evitar problemas de caché complejos.
* La comunicación entre microservicios se realiza a través de los nombres de servicio definidos en `docker-compose.yml` (ej. `http://productos-service:8081`).
* Las bases de datos PostgreSQL persistirán los datos en volúmenes Docker (`productos_data`, `inventario_data`).

## Detener los Servicios

Para detener y eliminar todos los contenedores y volúmenes creados por Docker Compose:
```bash
docker-compose down --volumes

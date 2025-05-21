# Usa una imagen base de OpenJDK para Java 17
FROM openjdk:21-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR construido de tu aplicación Spring Boot al contenedor
# Asume que tu JAR se genera en el directorio 'target' con un nombre similar a 'inventario-service-0.0.1-SNAPSHOT.jar'
# Verifica el nombre exacto de tu JAR después de un 'mvn clean install'
ARG JAR_FILE=target/inventario-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expone el puerto en el que la aplicación Spring Boot se ejecutará (configurado en application.yml)
EXPOSE 8082

# Define el comando para ejecutar la aplicación cuando el contenedor se inicie
ENTRYPOINT ["java", "-jar", "app.jar"]
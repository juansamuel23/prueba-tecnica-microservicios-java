#Uso de imagen base de OpenJDK para Java 21.
FROM openjdk:21-jdk-slim

#Directorio de trabajo dentro del contenedor.
WORKDIR /app

ARG JAR_FILE=target/productos-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

#Exponer puerto en la app spring boot, se ejcutara (8081)
EXPOSE 8081

#Cmanod para ejecutar la app cuando se inicie el contenedor.
ENTRYPOINT ["java","-jar","app.jar"]

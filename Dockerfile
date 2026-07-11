# 1. Etapa de compilación usando Maven y Java 17
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# Esta línea le da el permiso que Linux está pidiendo:
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# 2. Etapa de ejecución con una imagen oficial y ligera de Java 17
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
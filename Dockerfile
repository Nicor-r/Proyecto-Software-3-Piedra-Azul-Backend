# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Primero solo el pom: la capa de dependencias se reutiliza mientras el pom no cambie
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas se ejecutan en el pipeline de CI, no al construir la imagen
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# No ejecutar como root
RUN addgroup -S piedrazul && adduser -S piedrazul -G piedrazul
USER piedrazul

COPY --from=build /app/target/api-*.jar app.jar

# Render asigna el puerto en la variable PORT (ver server.port en application.properties)
EXPOSE 8080

# Limita el heap al 75 % de la memoria del contenedor (el plan gratuito de Render tiene 512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

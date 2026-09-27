# Fase 1: Build dell'applicazione --> eseguita già durante la CI di Jenkins
#FROM  maven:3.9-eclipse-temurin-17 AS builder
#WORKDIR /app
#COPY pom.xml .
#COPY src ./src
# Compila il progetto e crea il file JAR (escludendo i test per rapidità)
#RUN mvn clean package -DskipTests

# Fase 2: Creazione dell'immagine finale --> Docker recupera il .Jar da jenkins
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
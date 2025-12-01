# ----- Stage 1: Build -----
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests


# ----- Stage 2: Runtime -----
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/krpsim-1.0.jar app.jar

# Files that you want to run (scenarios) — corrected folder name
COPY krpsim ./krpsim

CMD ["java", "-jar", "app.jar", "krpsim/simple", "100"]


# mvn clean package -DskipTests

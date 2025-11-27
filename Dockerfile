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

# файл, которые ты хочешь запускать
COPY krpism ./krpism

CMD ["java", "-jar", "app.jar", "krpism/simple/simple.txt"]



# FROM openjdk:17
# WORKDIR /app
# COPY . .
# RUN apt update && apt install -y maven
# RUN mvn clean compile
# CMD ["java", "-cp", "target/classes", "krpsim.Krpsim", "krpism/simple/simple.txt"]

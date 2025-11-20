FROM openjdk:17
WORKDIR /app
COPY . .
RUN apt update && apt install -y maven
RUN mvn clean compile
CMD ["java", "-cp", "target/classes", "krpsim.Krpsim", "krpism/simple/simple.txt"]

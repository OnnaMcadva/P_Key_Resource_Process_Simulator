# P_Key_Resource_Process_Simulator

## Overview

P_Key_Resource_Process_Simulator is a Java-based simulator for process and resource management tasks. It allows you to run simulations based on input files provided in the `krpism/` folder.

## Features

- Simulate process/resource scenarios from structured input files
- Build and run as a command-line Java application or inside Docker
- Configurable input and runtime options

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Docker (optional, for containerized runs)

### Build & Run Locally

```bash
# Install dependencies and package
mvn clean package -DskipTests

# Run simulator
java -jar target/krpsim-1.0.jar krpism/simple/simple.txt
```

### Build & Run with Docker

```bash
docker build -t krpsim .
docker run --rm krpsim
```

By default, the container runs using the `krpism/simple/simple.txt` input file. You can replace this file with your own.

### Project Structure

- `src/`: Main Java source code
- `krpism/`: Folder for simulation input files (`simple/simple.txt` is the default)
- `Dockerfile`: Containerization setup for building and running
- `pom.xml`: Maven build configuration

### Example Usage

Edit or add input files in the `krpism/` directory as needed.

## Troubleshooting

- Ensure all referenced files exist (`krpism/simple/simple.txt`).
- If Docker build fails, check Maven output, missing dependencies, or incorrect paths.

## License & Contributing

Feel free to fork, contribute, or report issues!

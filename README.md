# P_Key_Resource_Process_Simulator

## Overview

P_Key_Resource_Process_Simulator is a Java-based simulator for process and resource management tasks. It allows you to run simulations using different scenario files from the `krpsim/` folder.

## Features

- Simulate process/resource scenarios from custom input files
- Build and run easily via Docker and Makefile
- Quickly test different scenarios by switching input files

## Project Structure

- `src/` — Java source code
- `krpsim/` — scenario files for testing (examples: `simple`, `pomme`, `inception`, etc.)
- `Dockerfile` — for containerized builds and runs
- `Makefile` — shortcuts for frequent Docker commands
- `portable_app/` — a prebuilt portable distribution (contains `krpsim-1.0.jar` and scenarios), optional
- `pom.xml` — Maven build configuration
- `.gitignore`, `README.md` — housekeeping

## Requirements

- Java 17+
- Maven
- Docker (optional, but recommended)

## Quick Start

### 1. Build with Maven (local, without Docker)

```bash
mvn clean package -DskipTests
```

### 2. Run Simulator Locally

Change the scenario file (`krpsim/simple`, `krpsim/pomme`, etc.) as you wish:

```bash
java -jar target/krpsim-1.0.jar krpsim/simple
java -jar target/krpsim-1.0.jar krpsim/pomme
```

### 3. Build & Run with Docker (using Makefile)

**Build the Docker image:**
```bash
make build
```

**Run the default scenario (`krpsim/simple`) in Docker:**
```bash
make run
```

**Open interactive shell in Docker (for manual tests):**
```bash
make shell
# Then inside container:
java -jar app.jar krpsim/pomme
java -jar app.jar krpsim/inception
# (and any other scenario file)
```

**Remove Docker image:**
```bash
make clean
```

## How to Test Different Scenarios

- All scenario files are in the `krpsim/` folder: `simple`, `pomme`, `inception`, `ikea`, `recre`, `steak`.
- Just change the file name after `krpsim/` when running the jar.
- You can create your own test files in this folder following the structure of others.

## Troubleshooting

- Check existence of scenario files before running commands!
- For Docker: make sure Docker is installed and running.
- For Java: use Java 17 or newer.

## Contributing & Support

Feel free to fork, contribute, or report issues!

---

Made with ❤️ for simulation experiments!

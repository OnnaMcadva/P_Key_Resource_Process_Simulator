

---

# P_Key_Resource_Process_Simulator

## Overview

P_Key_Resource_Process_Simulator is a Java-based simulator for process and resource management tasks. It allows you to run simulations using different scenario files from the `krpsim/` folder.

## Features

* Simulate process/resource scenarios from custom input files
* Build and run easily via Docker and Makefile
* Quickly test different scenarios by switching input files

## Project Structure

* `src/` — Java source code
* `krpsim/` — scenario files for testing (examples: `simple`, `pomme`, `inception`, etc.)
* `Dockerfile` — for containerized builds and runs
* `Makefile` — shortcuts for frequent Docker commands
* `portable_app/` — a prebuilt portable distribution (contains `krpsim-1.0.jar` and scenarios), optional
* `pom.xml` — Maven build configuration
* `.gitignore`, `README.md` — housekeeping

## Requirements

* Java 17+
* Maven
* Docker (optional but recommended)

## Installation (fast and reliable)

* Make sure JDK 17 (Eclipse Temurin / OpenJDK) is installed.
  On Windows, you can install JDK 17 using MSI or ZIP.
* Check versions in the terminal:

```powershell
java -version
mvn -v
```

* If `java` is not found, install JDK 17 and set `JAVA_HOME`. Example (PowerShell, current session):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
```

To set `JAVA_HOME` system-wide (Windows):

```powershell
[Environment]::SetEnvironmentVariable('JAVA_HOME','C:\Program Files\Java\jdk-17','Machine')
```

On Linux/macOS, add to `~/.bashrc` or `~/.zshrc`:

```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

## Quick Start

### 1. Build with Maven (local, without Docker)

```bash
mvn clean package -DskipTests
```

If you want to get a portable fat-jar (includes dependencies), Maven Shade is already configured — after a successful build the executable `krpsim-1.0.jar` will appear in `target/`.

### 2. Run Simulator Locally

Change the scenario file (`krpsim/simple`, `krpsim/pomme`, etc.) as needed:

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

**Open an interactive shell inside Docker (for manual tests):**

```bash
make shell
# Then inside the container:
java -jar app.jar krpsim/pomme
java -jar app.jar krpsim/inception
# (and any other scenario file)
```

**Remove Docker image:**

```bash
make clean
```

### Using the Makefile (universal way)

* `make run` — tries to run local `target/krpsim-1.0.jar` if it exists, otherwise builds a Docker image and runs inside a container.
  You can override scenario and steps:

```bash
make run SCENARIO=krpsim/pomme STEPS=200
```

* `make mvn-build` or `make jar` — runs `mvn clean package -DskipTests` and creates `target/krpsim-1.0.jar`.
* `make build` or `make docker-build` — builds Docker image `krpsim`.
* `make shell` — opens `/bin/bash` inside the image (useful for debugging).

If your machine does not have `make` (common on Windows), use `make.ps1` (PowerShell) from the project root; it mirrors the same targets:

```powershell
# Example (PowerShell)
.\make.ps1 mvn-build
.\make.ps1 run -Scenario krpsim/simple -Steps 100
.\make.ps1 build
```

`make.ps1` automatically uses the local jar if it exists or builds/runs the Docker image just like `make run`.

## How to Test Different Scenarios

* All scenario files are located in the `krpsim/` folder: `simple`, `pomme`, `inception`, `ikea`, `recre`, `steak`.
* Simply change the file name after `krpsim/` when running the jar.
* You can create your own test files in this folder following the structure of the existing ones.

## Troubleshooting

* Check that scenario files exist before running commands!
* For Docker: ensure Docker is installed and running.
* For Java: use Java 17 or newer.

### Quick Run Options (if you want to run on any machine)

* Locally (after `mvn package`):

```powershell
# Windows PowerShell
.\run.ps1 krpsim/simple 100

# Unix-like
./run.sh krpsim/simple 100
```

* Via Docker (most portable — Docker ensures the same environment):

```powershell
docker build -t krpsim .
docker run --rm krpsim
```

If you need to pass a different scenario via Docker:

```powershell
docker run --rm krpsim java -jar app.jar krpsim/pomme 100
```

## Contributing & Support

Feel free to fork, contribute, or report issues!

---

Made with ❤️ for simulation experiments!

---

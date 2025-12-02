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

## Installation (быстро и надёжно)

- Убедитесь, что установлен JDK 17 (Eclipse Temurin / OpenJDK). На Windows можно установить MSI/ZIP для JDK 17.
- Проверьте в терминале:

```powershell
java -version
mvn -v
```

- Если `java` не найден, нужно установить JDK 17 и задать `JAVA_HOME`. Пример (PowerShell, текущая сессия):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
```

Чтобы задать `JAVA_HOME` системно (в Windows):

```powershell
[Environment]::SetEnvironmentVariable('JAVA_HOME','C:\Program Files\Java\jdk-17','Machine')
```

На Linux/macOS добавьте в `~/.bashrc`/`~/.zshrc`:

```sh
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH="$JAVA_HOME/bin:$PATH"
```

## Quick Start

### 1. Build with Maven (local, without Docker)

```bash
mvn clean package -DskipTests
```

Если вы хотите получить переносимый "fat-jar" (включает зависимости), в проект уже добавлен Maven Shade — после успешной сборки в `target/` будет исполняемый `krpsim-1.0.jar`.

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

### Using the Makefile (универсально)

- `make run` — попробует запустить локальный `target/krpsim-1.0.jar`, если он есть; иначе соберёт Docker-образ и запустит внутри контейнера. Можно переопределить сценарий и шаги:

```bash
make run SCENARIO=krpsim/pomme STEPS=200
```

- `make mvn-build` или `make jar` — запустит `mvn clean package -DskipTests` и создаст `target/krpsim-1.0.jar`.
- `make build` или `make docker-build` — соберёт Docker-образ `krpsim`.
- `make shell` — откроет `/bin/bash` внутри образа (полезно для отладки).

Если на вашей машине нет `make` (часто на Windows), используйте `make.ps1` (PowerShell) из корня проекта, он повторяет те же цели:

```powershell
# Пример (PowerShell)
.\make.ps1 mvn-build
.\make.ps1 run -Scenario krpsim/simple -Steps 100
.\make.ps1 build
```

`make.ps1` автоматом использует локальный jar если он есть, или собирает и запускает Docker-образ как и `make run`.

## How to Test Different Scenarios

- All scenario files are in the `krpsim/` folder: `simple`, `pomme`, `inception`, `ikea`, `recre`, `steak`.
- Just change the file name after `krpsim/` when running the jar.
- You can create your own test files in this folder following the structure of others.

## Troubleshooting

- Check existence of scenario files before running commands!
- For Docker: make sure Docker is installed and running.
- For Java: use Java 17 or newer.

### Быстрые варианты запуска (если хотите запускать на любой машине)

- Локально (после `mvn package`):

```powershell
# Windows PowerShell
.\run.ps1 krpsim/simple 100

# Unix-like
./run.sh krpsim/simple 100
```

- Через Docker (самый портируемый способ — Docker обеспечивает одинаковую среду):

```powershell
docker build -t krpsim .
docker run --rm krpsim
```

Если нужно передать другой сценарий через Docker:

```powershell
docker run --rm krpsim java -jar app.jar krpsim/pomme 100
```

## Contributing & Support

Feel free to fork, contribute, or report issues!

---

Made with ❤️ for simulation experiments!

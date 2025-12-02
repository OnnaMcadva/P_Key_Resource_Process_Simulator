

IMAGE_NAME=krpsim
CONTAINER_NAME=krpsim_container

# default scenario and steps (can be overridden by passing SCENARIO/STEPS)
SCENARIO ?= krpsim/simple
STEPS ?= 100

.PHONY: help mvn-build jar build docker-build run run-local shell clean

help:
	@echo "Makefile targets (use 'make <target>')"
	@echo "  mvn-build    - build jar via Maven (requires JDK + Maven)"
	@echo "  jar          - same as mvn-build (keeps compatibility)"
	@echo "  docker-build - build Docker image"
	@echo "  build        - alias for docker-build"
	@echo "  run          - run using local jar if present, otherwise run Docker image"
	@echo "  run-local    - force run using local jar (requires target/krpsim-1.0.jar)"
	@echo "  shell        - open shell inside Docker image"
	@echo "  clean        - remove Docker image"

mvn-build:
	@echo "==> Running Maven package (may require JAVA_HOME set)"
	mvn clean package -DskipTests

jar: mvn-build

docker-build:
	@echo "==> Building Docker image $(IMAGE_NAME)"
	docker build -t $(IMAGE_NAME) .

build: docker-build

run: ## run: use local jar if exists, otherwise use Docker
	@if [ -f target/krpsim-1.0.jar ]; then \
		echo "Found target/krpsim-1.0.jar, running locally..."; \
		java -jar target/krpsim-1.0.jar $(SCENARIO) $(STEPS); \
	else \
		echo "Jar not found, running in Docker (will build image if needed)..."; \
		$(MAKE) build; \
		docker run --rm -it $(IMAGE_NAME) java -jar app.jar $(SCENARIO) $(STEPS); \
	fi

run-local:
	@if [ -f target/krpsim-1.0.jar ]; then \
		java -jar target/krpsim-1.0.jar $(SCENARIO) $(STEPS); \
	else \
		echo "Error: target/krpsim-1.0.jar not found. Run 'make mvn-build' first."; exit 1; \
	fi

shell: build
	docker run --rm -it $(IMAGE_NAME) /bin/bash

clean:
	@echo "Removing Docker image (if exists)"
	-docker rmi $(IMAGE_NAME) || true


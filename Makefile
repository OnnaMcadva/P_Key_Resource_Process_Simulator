
IMAGE_NAME=krpsim
CONTAINER_NAME=krpsim_container

# default scenario and delay (can be overridden by passing SCENARIO/DELAY)
SCENARIO ?= krpsim/simple
DELAY ?= 100

build:
	docker build -t $(IMAGE_NAME) .

run: build
	docker run --rm -it $(IMAGE_NAME) java -jar app.jar $(SCENARIO) $(DELAY)

shell: build
	docker run --rm -it $(IMAGE_NAME) /bin/bash

clean:
	docker rmi $(IMAGE_NAME) || true

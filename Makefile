IMAGE_NAME=krpsim
CONTAINER_NAME=krpsim_container

build:
	docker build -t $(IMAGE_NAME) .

run: build
	docker run --rm -it $(IMAGE_NAME)

shell: build
	docker run --rm -it $(IMAGE_NAME) /bin/bash

clean:
	docker rmi $(IMAGE_NAME) || true

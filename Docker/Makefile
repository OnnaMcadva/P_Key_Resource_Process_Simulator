name = krpsim

all: src files
	@printf "Up ${name}:\n"
	@COMPOSE_BAKE=true docker compose -f docker-compose.yml up -d

build: src files
	@printf "Build ${name}:\n"
	@COMPOSE_BAKE=true docker compose --build

down:
	@printf "Down ${name} (stop and remove containers, networks):\n"
	@COMPOSE_BAKE=true docker compose -f docker-compose.yml down

re: down src files
	@printf "Rebuild ${name}:\n"
	@COMPOSE_BAKE=true docker compose -f docker-compose.yml up -d --build

src:
	@rm -fR ${name}/src
	@mkdir -p ${name}/src
	@cp -a ../src/. ./${name}/src/

files:
	@rm -fR ${name}/files
	@mkdir -p ${name}/files
	@cp -a ../krpsim/. ./${name}/files/

clean: down
	@printf "Clean ${name}:\n"
	@docker system prune -a

fclean: clean
	@printf "Total clean of all configurations docker\n"
	@docker volume prune --force

env:
	@printf "Show ENV ${name}:\n"
	@docker compose config --environment

bash:
	@docker exec -it ${name} bash

.PHONY  : all build down re src files clean fclean env bash

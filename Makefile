.PHONY: build run clean

build:
	mvn -q clean package

run:
	java -jar target/krpsim-1.0.jar

clean:
	mvn -q clean


#// make build
#// make run

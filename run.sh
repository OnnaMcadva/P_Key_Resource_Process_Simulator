#!/bin/sh
SCENARIO=${1:-krpsim/simple}
STEPS=${2:-100}
JAR=target/krpsim-1.0.jar
if [ ! -f "$JAR" ]; then
  echo "Jar not found: $JAR. Run 'mvn clean package -DskipTests' first."
  exit 1
fi

java -jar "$JAR" "$SCENARIO" "$STEPS"

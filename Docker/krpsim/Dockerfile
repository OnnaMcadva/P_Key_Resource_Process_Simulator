FROM eclipse-temurin:17
RUN mkdir -p /opt/krpsim
RUN mkdir -p /opt/krpsim/src
COPY src /opt/krpsim/src/
WORKDIR /opt/krpsim/src/main/java/
RUN javac -d ../out $(find . -name "*.java")
WORKDIR /opt/krpsim/src/main/
RUN jar cfm ../../krpsim.jar manifest.txt -C out .
WORKDIR /opt/krpsim/
RUN echo '#!/bin/sh' > krpsim
RUN echo 'exec java -jar "$(dirname "$0")/krpsim.jar" "$@"' >> krpsim
RUN chmod +x krpsim
ENTRYPOINT ["sh", "-c", "sleep infinity"]

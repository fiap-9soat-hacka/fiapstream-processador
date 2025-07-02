FROM ubuntu:22.04

RUN apt-get update && \
    apt-get install -y openjdk-21-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

RUN apt-get install -y ffmpeg

COPY src /app/src
COPY pom.xml /app

WORKDIR /app
RUN mvn clean install -U

COPY /app/target/quarkus/* /deployments/

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]

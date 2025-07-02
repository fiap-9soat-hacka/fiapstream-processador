FROM eclipse-temurin:21-jdk

COPY src /app/src
COPY pom.xml /app

WORKDIR /app

RUN apt update && apt install -y ffmpeg

RUN mvn clean install -U

COPY /app/target/quarkus/* /deployments/

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]

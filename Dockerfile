FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

RUN apt update && apt install -y maven ffmpeg

RUN mvn clean install -U

COPY /app/target/quarkus/* /deployments/

ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]

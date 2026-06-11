ARG JRE_IMAGE=eclipse-temurin:25-jre
ARG JAR_FILE=target/gym-crm-1.0-SNAPSHOT.jar

FROM ${JRE_IMAGE}

ARG JAR_FILE

WORKDIR /app
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

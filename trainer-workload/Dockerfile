FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/trainer-workload-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

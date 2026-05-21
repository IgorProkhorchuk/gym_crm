ARG MAVEN_IMAGE=docker.io/library/maven:3.9.14-amazoncorretto-25
ARG TOMCAT_IMAGE=tomcat:11.0-jdk25

FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace

COPY pom.xml .

COPY src ./src

RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM ${TOMCAT_IMAGE}

COPY --from=build /workspace/target/gym-crm-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

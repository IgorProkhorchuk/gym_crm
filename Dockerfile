ARG TOMCAT_IMAGE=tomcat:11.0-jdk25
ARG WAR_FILE=target/gym-crm-1.0-SNAPSHOT.war

FROM ${TOMCAT_IMAGE}

ARG WAR_FILE

COPY ${WAR_FILE} /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

ARG MAVEN_IMAGE=docker.io/library/maven:3.9.14-amazoncorretto-25
ARG RUNTIME_IMAGE=docker.io/library/amazoncorretto:25-alpine

FROM ${MAVEN_IMAGE} AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package dependency:copy-dependencies \
        -DincludeScope=runtime \
        -DoutputDirectory=target/dependency

FROM ${RUNTIME_IMAGE} AS runtime

RUN set -eux; \
    if command -v addgroup >/dev/null 2>&1; then \
        addgroup -S gymcrm; \
        adduser -S -G gymcrm -h /app -s /sbin/nologin gymcrm; \
    else \
        groupadd --system gymcrm; \
        useradd --system --gid gymcrm --home-dir /app --shell /sbin/nologin gymcrm; \
    fi

WORKDIR /app

ENV DB_URL=jdbc:postgresql://gym-pgpool:5432/gym_crm \
    DB_USERNAME=gym_user \
    DB_DRIVER=org.postgresql.Driver \
    JAVA_OPTS=""

COPY --from=build --chown=gymcrm:gymcrm /workspace/target/gym-crm-*.jar ./app.jar
COPY --from=build --chown=gymcrm:gymcrm /workspace/target/dependency ./lib

USER gymcrm

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -cp 'app.jar:lib/*' com.epam.gymcrm.Main"]

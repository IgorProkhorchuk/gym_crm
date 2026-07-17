package com.epam.gymcrm.workload.integrationbdd;

import io.cucumber.spring.CucumberContextConfiguration;
import java.io.IOException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CucumberIntegrationSpringConfiguration {

  private static final int MONGO_PORT = 27017;
  private static final DockerImageName MONGO_IMAGE = DockerImageName.parse("mongo:7");
  private static final GenericContainer<?> MONGO =
      new GenericContainer<>(MONGO_IMAGE)
          .withExposedPorts(MONGO_PORT)
          .withCommand("mongod", "--replSet", "rs0", "--bind_ip_all");

  static {
    MONGO.start();
    initializeReplicaSet();
  }

  @DynamicPropertySource
  static void mongoProperties(DynamicPropertyRegistry registry) {
    registry.add(
        "spring.mongodb.uri",
        () ->
            "mongodb://"
                + MONGO.getHost()
                + ":"
                + MONGO.getMappedPort(MONGO_PORT)
                + "/trainer_workload_cucumber_it?directConnection=true&replicaSet=rs0");
  }

  private static void initializeReplicaSet() {
    try {
      MONGO.execInContainer(
          "mongosh",
          "--quiet",
          "--eval",
          "try { rs.status().ok } catch (e) { "
              + "rs.initiate({_id: 'rs0', members: [{ _id: 0, host: 'localhost:27017' }]}) }");
      waitForWritablePrimary();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to initialize MongoDB replica set", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while initializing MongoDB replica set", exception);
    }
  }

  private static void waitForWritablePrimary() throws IOException, InterruptedException {
    for (int attempt = 0; attempt < 30; attempt++) {
      ExecResult result =
          MONGO.execInContainer(
              "mongosh",
              "--quiet",
              "--eval",
              "db.hello().isWritablePrimary ? 1 : 0");
      if (result.getStdout().trim().equals("1")) {
        return;
      }
      Thread.sleep(1000);
    }

    throw new IllegalStateException("MongoDB replica set did not elect a primary");
  }
}

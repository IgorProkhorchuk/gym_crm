package com.epam.gymcrm.workload.bdd;

import com.epam.gymcrm.workload.service.TrainerWorkloadService;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.data.mongodb.repositories.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration,"
            + "org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration"
    })
public class CucumberSpringConfiguration {

  @MockitoBean private TrainerWorkloadService trainerWorkloadService;
}

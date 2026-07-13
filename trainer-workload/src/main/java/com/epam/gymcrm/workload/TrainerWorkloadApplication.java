package com.epam.gymcrm.workload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@SpringBootApplication
public class TrainerWorkloadApplication {
  public static void main(String[] args) {
    SpringApplication.run(TrainerWorkloadApplication.class, args);
  }
}

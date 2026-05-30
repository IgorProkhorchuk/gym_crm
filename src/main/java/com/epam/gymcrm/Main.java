package com.epam.gymcrm;

import com.epam.gymcrm.config.RootConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {

  public static void main(String[] args) {
    new AnnotationConfigApplicationContext(RootConfig.class);
  }
}

package com.epam.gymcrm;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@ComponentScan("com.epam.gymcrm")
@Configuration
@PropertySource("classpath:application.properties")
public class Main {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    public static void main(String[] args) {
        new AnnotationConfigApplicationContext(Main.class);
    }
}

package com.autumnus.spring_boot_starter_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringBootStarterTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootStarterTemplateApplication.class, args);
    }

}

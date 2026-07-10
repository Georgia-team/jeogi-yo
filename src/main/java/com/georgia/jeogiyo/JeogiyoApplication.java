package com.georgia.jeogiyo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditingConfig", modifyOnCreate = false)
@SpringBootApplication
public class JeogiyoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JeogiyoApplication.class, args);
    }
}
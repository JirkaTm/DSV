package org.example.dsva;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan({"org.example.dsva.controller", "org.example.dsva.service", "org.example.dsva.model"})
@EntityScan("org.example.dsva.")
public class DsVaApplication {
    public static void main(String[] args) {
        SpringApplication.run(DsVaApplication.class, args);
    }
}

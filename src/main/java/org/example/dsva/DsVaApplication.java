package org.example.dsva;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan({"org.example.dsva.controller", "org.example.dsva.service", "org.example.dsva.model"})
@EntityScan("org.example.dsva.")
public class DsVaApplication {
    public static int cwsNum;

    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("0")) {
            System.out.println("No argument provided. Shutting down...");
            System.exit(1);
        }
        for(String arg:args) {
            try {
                cwsNum = Integer.parseInt(arg);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid argument: " + arg);
                System.exit(1);
            }
        }
        SpringApplication.run(DsVaApplication.class, args);
    }
}

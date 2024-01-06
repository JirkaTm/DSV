package org.example.dsva.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

@org.springframework.stereotype.Service
public class InputService {

    @Autowired
    private Service service;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            CompletableFuture.runAsync(() -> {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    String input = scanner.nextLine();
                    if("".equals(input)){
                        continue;
                    }
                    if("--exit".equals(input)){
                        System.exit(0);
                    }
                    service.sendMessage(input);
                }
            });
        };
    }
}

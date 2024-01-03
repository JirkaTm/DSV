package org.example.dsva.controller;

import org.example.dsva.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.concurrent.CompletableFuture;

@RestController
public class EndpointController {

    private static final Logger logger = LoggerFactory.getLogger(EndpointController.class);
    @Autowired
    private Service service;

    @PostMapping("/election")
    public ResponseEntity<String> electionReceived() {
        logger.info("Election message received");
        CompletableFuture.runAsync(service::start_election);
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }

    @PostMapping("/victory")
    public ResponseEntity<String> victoryReceived(@RequestParam Integer id) {
        logger.info("Victory message received, id: " + id);
        if (service.setLeader(id)) {
            return ResponseEntity.status(HttpStatus.OK).body("OK!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad request!");
        }
    }

    @PostMapping("/message")
    public ResponseEntity<String> messageReceived(@RequestParam Integer id, @RequestBody String message) {
        logger.info("Chat message received, id: " + id + ", message: " + message);
        service.spreadMessage(id, message);
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }
}


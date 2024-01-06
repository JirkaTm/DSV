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

    @PostMapping("/exit")
    public ResponseEntity<String> exitReceived() {
        logger.info("Exit message received. Exiting...");
        System.exit(0);
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }

    @PostMapping("/exit/force")
    public ResponseEntity<String> forceExitReceived() {
        service.setForceExit(true);
        logger.info("Force exit message received. Exiting...");
        System.exit(0);
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }

    @PostMapping("/delay")
    public ResponseEntity<String> setDelay(@RequestParam Integer time) {
        logger.info("Delay message received. Setting delay to " + time + "ms");
        service.setDelay(time);
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }
}


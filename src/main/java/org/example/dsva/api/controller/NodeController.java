package org.example.dsva.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeController {



//    @GetMapping("/user")
//    public User getUser(@RequestParam Integer id){
//        Optional<User> user = userService.getUser(id);
//        return (User) user.orElse(null);
//    }

    @GetMapping("/alive")
    public ResponseEntity<String> isAlive(){
        return ResponseEntity.status(HttpStatus.OK).body("OK!");
    }
}
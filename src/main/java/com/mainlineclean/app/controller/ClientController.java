package com.mainlineclean.app.controller;

import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    @GetMapping("/clients")
    public ResponseEntity<Client> getClients() {
        return null;
    }

    @PostMapping("/clients")
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        return null;
    }
}

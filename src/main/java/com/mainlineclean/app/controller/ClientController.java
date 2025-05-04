package com.mainlineclean.app.controller;

import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @PostMapping("/client")
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @DeleteMapping("/client")
    public ResponseEntity<String> updateClient(@RequestBody Client client) {
        clientService.deleteClient(client);
        return ResponseEntity.ok("OK");
    }
}
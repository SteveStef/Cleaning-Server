package com.mainlineclean.app.controller;

import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.service.ClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/client")
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        Client c = clientService.createClient(client);
        return ResponseEntity.ok(c);
    }

    @DeleteMapping("/client")
    public ResponseEntity<String> updateClient(@RequestBody Client client) {
        clientService.deleteClient(client);
        return ResponseEntity.ok("OK");
    }
}
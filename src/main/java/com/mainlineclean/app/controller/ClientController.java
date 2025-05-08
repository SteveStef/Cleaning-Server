package com.mainlineclean.app.controller;

import com.mainlineclean.app.dto.Records;
import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.service.ClientService;
import com.mainlineclean.app.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Slf4j
public class ClientController {
    private final ClientService clientService;
    private final EmailService emailService;

    public ClientController(ClientService clientService, EmailService emailService) {
        this.clientService = clientService;
        this.emailService = emailService;
    }

    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getClients() {
        List<Client> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/client")
    public ResponseEntity<Client> addClient(@RequestBody Client client) {
        return ResponseEntity.ok(clientService.createClient(client));
    }

    @DeleteMapping("/clients")
    public ResponseEntity<String> updateClient(@RequestBody List<Long> clientIds) {
        clientService.deleteClients(clientIds);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/clients/email")
    public ResponseEntity<String> sendEmailToClients(@RequestBody Records.ClientEmailBody data) {
        emailService.sendEmailToClients(data.clientEmails(), data.message());
        return ResponseEntity.ok("OK");
    }
}
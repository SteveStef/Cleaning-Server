package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.repository.ClientRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ClientService {
    private final EmailService emailService;
    private final ClientRepo clientRepo;

    public ClientService(ClientRepo clientRepo, EmailService emailService) {
        this.clientRepo = clientRepo;
        this.emailService = emailService;
    }

    public void createClient(Appointment appointment) {
        try {
            Client client = new Client(appointment);
            clientRepo.save(client);
        } catch(Exception e) {
            log.warn("Client already exists for appointment {}", appointment.getId());
        }
    }

    public Client createClient(Client client) {
        try {
            return clientRepo.save(client);
        } catch(Exception e) {
            log.warn("Tried to add a new client that already exists: {} {}", client.getEmail(), e.getMessage());
            throw new DataIntegrityViolationException(e.getMessage());
        }
    }

    public Client getClient(String email) {
        return clientRepo.findByEmail(email).orElseThrow(EntityNotFoundException::new);
    }

    public List<Client> getAllClients() {
       return clientRepo.findAll();
    }

    public void deleteClients(List<Long> clients) {
        clientRepo.deleteAllById(clients);
    }

    public void sendEmailsToClients(List<Client> clients) {
        //emailService.sendTemplatedEmail();
    }
}

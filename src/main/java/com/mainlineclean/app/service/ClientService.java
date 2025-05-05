package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.Client;
import com.mainlineclean.app.repository.ClientRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
            System.out.println(e.toString());
        }
    }

    public Client createClient(Client client) {
        return clientRepo.save(client);
    }

    public Client getClient(String email) {
        return clientRepo.findByEmail(email).orElseThrow(EntityNotFoundException::new);
    }

    public List<Client> getAllClients() {
       return clientRepo.findAll();
    }

    public void deleteClient(Client client) {
        clientRepo.delete(client);
    }

    public void sendEmailsToClients(List<Client> clients) {
        //emailService.sendTemplatedEmail();
    }
}

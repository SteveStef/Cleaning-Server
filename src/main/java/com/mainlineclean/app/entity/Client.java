package com.mainlineclean.app.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Table(name = "clients", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "zipcode")
    private String zipcode;

    public Client() {}

    public Client(Long id, String name, String address, String email, String phone, String zipcode) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.zipcode = zipcode;
    }

    public Client(Appointment appointment) {
        this.name = appointment.getClientName();
        this.address = appointment.getAddress();
        this.email = appointment.getEmail();
        this.phone = appointment.getPhone();
        this.zipcode = appointment.getZipcode();
    }
}

package com.mainlineclean.app.entity;

import java.util.Date;
import java.util.Random;

import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.model.Time;
import jakarta.persistence.*;

@Entity
@Table(name = "appointment")
public class Appointment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "client_name")
  private String clientName;

  @Column(name = "email")
  private String email;

  @Column(name = "phone")
  private String phone;

  @Column(name = "city")
  private String city;

  @Enumerated(EnumType.STRING)
  @Column(name = "service")
  private ServiceType service;

  @Column(name = "appointment_date")
  private Date appointmentDate;

  @Column(name = "created_at")
  private Date createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "time")
  private Time time;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private Status status;

  @Column(name = "address")
  private String address;

  @Column(name="notes", columnDefinition="TEXT")
  private String notes;

  @Column(name = "orderId")
  private String orderId;

  @Column(name = "captureId")
  private String captureId;

  @Column(name = "chargedAmount")
  private String chargedAmount;

  @Column(name = "paypalFee")
  private String paypalFee;

  @Column(name = "netAmount")
  private String netAmount;

  @Column(name = "bookingId")
  private String bookingId;

  @PrePersist
  protected void onCreate() {
    this.createdAt = new Date();
    this.status = Status.CONFIRMED;
    this.bookingId = this.generateBookingId();
  }

  private String generateBookingId() {
    StringBuilder bID = new StringBuilder("BK-");
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    Random random = new Random();
    for (int i = 0; i < 5; i++) {
        bID.append(chars.charAt(random.nextInt(chars.length())));
    }
    bID.append("-");
    for (int i = 0; i < 3; i++) {
      bID.append(chars.charAt(random.nextInt(chars.length())));
    }
    return bID.toString();
  }

  // Default constructor
  public Appointment() {}


  public Appointment(Long id, String clientName, String email, String phone, ServiceType service, Date appointmentDate, Date createdAt, Time time, Status status, String address, String notes, String orderId, String captureId, String chargedAmount, String paypalFee, String netAmount, String bookingId, String city) {
    this.id = id;
    this.clientName = clientName;
    this.email = email;
    this.phone = phone;
    this.service = service;
    this.appointmentDate = appointmentDate;
    this.createdAt = createdAt;
    this.time = time;
    this.status = status;
    this.address = address;
    this.notes = notes;
    this.orderId = orderId;
    this.captureId = captureId;
    this.chargedAmount = chargedAmount;
    this.paypalFee = paypalFee;
    this.netAmount = netAmount;
    this.bookingId = bookingId;
    this.city = city;
  }

  // Getters and Setters
  public String getBookingId(){return this.bookingId;}

  public void setBookingId(String bookingId){ this.bookingId = bookingId; }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public ServiceType getService() {
    return service;
  }

  public void setService(ServiceType serviceType) {
    this.service = serviceType;
  }

  public Date getAppointmentDate() {
    return appointmentDate;
  }

  public void setAppointmentDate(Date appointmentDate) {
    this.appointmentDate = appointmentDate;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public Time getTime() {
    return time;
  }

  public void setTime(Time time) {
    this.time = time;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getChargedAmount() {
    return chargedAmount;
  }

  public void setChargedAmount(String chargedAmount) {
    this.chargedAmount = chargedAmount;
  }

  public String getPaypalFee() {
    return paypalFee;
  }

  public void setPaypalFee(String paypalFee) {
    this.paypalFee = paypalFee;
  }

  public String getNetAmount() {
    return netAmount;
  }

  public void setNetAmount(String netAmount) {
    this.netAmount = netAmount;
  }

  public String getCaptureId() {
    return captureId;
  }

  public void setCaptureId(String captureId) {
    this.captureId = captureId;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }



  @Override
  public String toString() {
    return "Appointment{" +
            "id=" + id +
            ", clientName='" + clientName + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", service='" + service + '\'' +
            ", appointmentDate=" + appointmentDate +
            ", createdAt=" + createdAt +
            ", time='" + time + '\'' +
            ", status='" + status + '\'' +
            ", address='" + address + '\'' +
            ", notes='" + notes + '\'' +
            ", orderId='" + orderId + '\'' +
            ", captureId='" + captureId + '\'' +
            ", chargedAmount='" + chargedAmount + '\'' +
            ", paypalFee='" + paypalFee + '\'' +
            ", netAmount='" + netAmount + '\'' +
            ", bookingId='" + bookingId + '\'' +
            ", city='" + city + '\'' +
            '}';
  }
}

package com.mainlineclean.app.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.model.Time;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "appointment", uniqueConstraints = @UniqueConstraint(columnNames = "bookingId"))
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

  @Column(name = "zipcode")
  private String zipcode;

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

  @Column(
          name = "charged_amount",
          precision = 19,
          scale = 2,
          nullable = false,
          columnDefinition = "DECIMAL(19,2) DEFAULT 0.00"
  )
  private BigDecimal chargedAmount = BigDecimal.ZERO;

  @Column(
          name = "paypal_fee",
          precision = 19,
          scale = 2,
          nullable = false,
          columnDefinition = "DECIMAL(19,2) DEFAULT 0.00"
  )
  private BigDecimal paypalFee = BigDecimal.ZERO;

  @Column(
          name = "gross_amount",
          precision = 19,
          scale = 2,
          nullable = false,
          columnDefinition = "DECIMAL(19,2) DEFAULT 0.00"
  )
  private BigDecimal grossAmount = BigDecimal.ZERO;

  @Column(name = "bookingId", nullable = false, unique = true)
  private String bookingId;

  @Column(name="smsConsent")
  private boolean smsConsent;

  @Column(name="squareFeet")
  private int squareFeet;

  @Column(name = "application_fee", precision = 19, scale = 2, nullable = false)
  @ColumnDefault("0.00")
  private BigDecimal applicationFee = BigDecimal.ZERO;

  // Default constructor
  public Appointment() {}

  public Appointment(Long id, String clientName, String email, String phone, String zipcode, ServiceType service, Date appointmentDate, Date createdAt, Time time, Status status, String address, String notes, String orderId, String captureId, BigDecimal chargedAmount, BigDecimal paypalFee, BigDecimal grossAmount, String bookingId, boolean smsConsent, int squareFeet, BigDecimal applicationFee) {
    this.id = id;
    this.clientName = clientName;
    this.email = email;
    this.phone = phone;
    this.zipcode = zipcode;
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
    this.grossAmount = grossAmount;
    this.bookingId = bookingId;
    this.smsConsent = smsConsent;
    this.squareFeet = squareFeet;
    this.applicationFee = applicationFee;
  }

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

  public String getZipcode() {
    return zipcode;
  }

  public void setZipcode(String zipcode) {
    this.zipcode = zipcode;
  }

  public ServiceType getService() {
    return service;
  }

  public void setService(ServiceType service) {
    this.service = service;
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

  public String getCaptureId() {
    return captureId;
  }

  public void setCaptureId(String captureId) {
    this.captureId = captureId;
  }

  public BigDecimal getChargedAmount() {
    return chargedAmount;
  }

  public void setChargedAmount(BigDecimal chargedAmount) {
    this.chargedAmount = chargedAmount;
  }

  public BigDecimal getPaypalFee() {
    return paypalFee;
  }

  public void setPaypalFee(BigDecimal paypalFee) {
    this.paypalFee = paypalFee;
  }

  public BigDecimal getGrossAmount() {
    return grossAmount;
  }

  public void setGrossAmount(BigDecimal grossAmount) {
    this.grossAmount = grossAmount;
  }

  public String getBookingId() {
    return bookingId;
  }

  public void setBookingId(String bookingId) {
    this.bookingId = bookingId;
  }

  public boolean isSmsConsent() {
    return smsConsent;
  }

  public void setSmsConsent(boolean smsConsent) {
    this.smsConsent = smsConsent;
  }

  public int getSquareFeet() {
    return squareFeet;
  }

  public void setSquareFeet(int squareFeet) {
    this.squareFeet = squareFeet;
  }

  public BigDecimal getApplicationFee() {
    return applicationFee;
  }

  public void setApplicationFee(BigDecimal applicationFee) {
    this.applicationFee = applicationFee;
  }

  @Override
  public String toString() {
    return "Appointment{" +
            "id=" + id +
            ", clientName='" + clientName + '\'' +
            ", email='" + email + '\'' +
            ", phone='" + phone + '\'' +
            ", zipcode='" + zipcode + '\'' +
            ", service=" + service +
            ", appointmentDate=" + appointmentDate +
            ", createdAt=" + createdAt +
            ", time=" + time +
            ", status=" + status +
            ", address='" + address + '\'' +
            ", notes='" + notes + '\'' +
            ", orderId='" + orderId + '\'' +
            ", captureId='" + captureId + '\'' +
            ", chargedAmount=" + chargedAmount +
            ", paypalFee=" + paypalFee +
            ", grossAmount=" + grossAmount +
            ", bookingId='" + bookingId + '\'' +
            ", smsConsent=" + smsConsent +
            ", squareFeet=" + squareFeet +
            ", applicationFee='" + applicationFee + '\'' +
            '}';
  }
}

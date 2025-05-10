package com.mainlineclean.app.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.State;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.model.Time;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@ToString
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
  @Column(name="state")
  private State state;

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

  @Column(name = "charged_amount", precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
  private BigDecimal chargedAmount = BigDecimal.ZERO;

  @Column(name = "paypal_fee", precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
  private BigDecimal paypalFee = BigDecimal.ZERO;

  @Column(name = "profit", precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
  private BigDecimal profit = BigDecimal.ZERO;

  @Column(name = "gross_amount", precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
  private BigDecimal grossAmount = BigDecimal.ZERO;

  @Column(name = "sales_tax", precision = 19, scale = 2, nullable = false, columnDefinition = "DECIMAL(19,2) DEFAULT 0.00")
  private BigDecimal salesTax = BigDecimal.ZERO;

  @Column(name = "bookingId", nullable = false, unique = true)
  private String bookingId;

  @Column(name="squareFeet")
  private int squareFeet;

  @Column(name = "application_fee", precision = 19, scale = 2, nullable = false)
  @ColumnDefault("0.00")
  private BigDecimal applicationFee = BigDecimal.ZERO;

  // Default constructor
  public Appointment() {}

  public Appointment(Long id, String clientName, String email, String phone, String zipcode, State state, ServiceType service, Date appointmentDate, Date createdAt, Time time, Status status, String address, String notes, String orderId, String captureId, BigDecimal chargedAmount, BigDecimal paypalFee, BigDecimal profit, BigDecimal grossAmount, BigDecimal salesTax, String bookingId, int squareFeet, BigDecimal applicationFee) {
    this.id = id;
    this.clientName = clientName;
    this.email = email;
    this.phone = phone;
    this.zipcode = zipcode;
    this.state = state;
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
    this.profit = profit;
    this.grossAmount = grossAmount;
    this.salesTax = salesTax;
    this.bookingId = bookingId;
    this.squareFeet = squareFeet;
    this.applicationFee = applicationFee;
  }
}

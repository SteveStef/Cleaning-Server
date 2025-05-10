package com.mainlineclean.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
@Table(name = "payment_intent")
public class PaymentIntent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "order_id", unique = true)
  private String orderId;

  @Column(name = "request_id")
  private String requestId = UUID.randomUUID().toString();

  @Column(name = "price", precision = 19, scale = 2)
  private BigDecimal price;

  public PaymentIntent() {}
}

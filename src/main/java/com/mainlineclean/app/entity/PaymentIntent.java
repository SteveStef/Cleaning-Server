package com.mainlineclean.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

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

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  @Override
  public String toString() {
    return "PaymentIntent{" +
            "id=" + id +
            ", orderId='" + orderId + '\'' +
            ", requestId='" + requestId + '\'' +
            ", price='" + price + '\'' +
            '}';
  }
}

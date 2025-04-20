package com.mainlineclean.app.entity;

import java.util.Date;

import com.mainlineclean.app.model.ServiceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "review")
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "client_name")
  private String clientName;

  @Column(name = "stars")
  private int stars;

  @Column(name = "location")
  private String location;

  @Column(name = "service")
  private ServiceType service;

  @Lob
  @Column(name = "content", columnDefinition = "LONGTEXT")
  private String content;

  @Column(name = "created_at")
  private Date createdAt;

  public Review() {}

  // This method is called automatically before the entity is persisted.
  @PrePersist
  protected void onCreate() {
    this.createdAt = new Date();
  }

  // Getters and Setters

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

  public int getStars() {
    return stars;
  }

  public void setStars(int stars) {
    this.stars = stars;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public ServiceType getService() {
    return service;
  }

  public void setService(ServiceType service) {
    this.service = service;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return "Review{" +
            "id=" + id +
            ", clientName='" + clientName + '\'' +
            ", stars=" + stars +
            ", location='" + location + '\'' +
            ", service='" + service + '\'' +
            ", content='" + content + '\'' +
            ", createdAt=" + createdAt +
            '}';
  }
}

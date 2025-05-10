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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
}

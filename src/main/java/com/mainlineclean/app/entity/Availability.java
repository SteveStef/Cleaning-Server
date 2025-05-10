package com.mainlineclean.app.entity;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@ToString
@Slf4j
@Entity
@Table(name = "availability")
public class Availability {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String date; // in the form yyyy-MM-dd

  @Column(name = "expiration_date")
  private Date expirationDate;

  @Column(name = "morning")
  private boolean morning;

  @Column(name = "afternoon")
  private boolean afternoon;

  @Column(name = "night")
  private boolean night;

  @Column(name = "is_available")
  private boolean isAvailable;

  public Availability() {}

  public Availability(String date, boolean morning, boolean afternoon, boolean night, boolean isAvailable) {
    this.date = date;
    this.morning = morning;
    this.afternoon = afternoon;
    this.night = night;
    this.isAvailable = isAvailable;
    syncExpirationDate();
  }

  // This method will be called before the entity is persisted or updated.
  @PrePersist
  @PreUpdate
  private void syncExpirationDate() {
    if (this.date != null) {
      try {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.expirationDate = sdf.parse(this.date);
      } catch (ParseException e) {
          log.error("Error parsing date string: {} {}", this.date, e.getMessage());
      }
    }
  }
}

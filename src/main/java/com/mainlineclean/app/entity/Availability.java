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

  // Getters and Setters
  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
    syncExpirationDate();
  }

  public Date getExpirationDate() {
    return expirationDate;
  }

  public void setExpirationDate(Date expirationDate) {
    this.expirationDate = expirationDate;
  }

  public boolean isMorning() {
    return morning;
  }

  public void setMorning(boolean morning) {
    this.morning = morning;
  }

  public boolean isAfternoon() {
    return afternoon;
  }

  public void setAfternoon(boolean afternoon) {
    this.afternoon = afternoon;
  }

  public boolean isNight() {
    return night;
  }

  public void setNight(boolean night) {
    this.night = night;
  }

  public boolean isAvailable() {
    return isAvailable;
  }

  public void setAvailable(boolean isAvailable) {
    this.isAvailable = isAvailable;
  }

  @Override
  public String toString() {
    return "Availability{" +
      "date='" + date + '\'' +
      ", expirationDate=" + expirationDate +
      ", morning=" + morning +
      ", afternoon=" + afternoon +
      ", night=" + night +
      ", isAvailable=" + isAvailable +
      '}';
  }

  // This method will be called before the entity is persisted or updated.
  @PrePersist
  @PreUpdate
  private void syncExpirationDate() {
    if (this.date != null) {
      try {
        // Parse the date string into a Date object.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        this.expirationDate = sdf.parse(this.date);
      } catch (ParseException e) {
        // Log or handle the exception as needed.
        e.printStackTrace();
      }
    }
  }
}

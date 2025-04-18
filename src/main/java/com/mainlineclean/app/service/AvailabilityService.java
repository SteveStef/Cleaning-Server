package com.mainlineclean.app.service;

import org.springframework.transaction.annotation.Transactional;

import com.mainlineclean.app.entity.Availability;
import com.mainlineclean.app.exception.AvailabilityException;

import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.repository.AvailabilityRepo;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class AvailabilityService {

  private final AvailabilityRepo availabilityRepo;

  public AvailabilityService(AvailabilityRepo  availabilityRepo) {
    this.availabilityRepo = availabilityRepo;
  }

  @Transactional
  public List<Availability> createAvailability(Availability[] availabilities) {
    availabilityRepo.deleteAll();
    List<Availability> availabilityList = new ArrayList<>(Arrays.asList(availabilities));
    availabilityList.removeIf(availability -> {
      LocalDate date = LocalDate.parse(availability.getDate()); // yyyy-MM-dd format
      return date.isBefore(LocalDate.now());
    });
    return availabilityRepo.saveAll(availabilityList);
  }

  public void updateAvailability(Appointment app) throws AvailabilityException {
    Date date = app.getAppointmentDate();
    String time = app.getTime().toLowerCase(); // expected values: "morning, 8:am-11am", "afternoon", "night"
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String formattedDate = sdf.format(date);
    Optional<Availability> availabilityOpt = availabilityRepo.findById(formattedDate);
    if (availabilityOpt.isEmpty()) {
      throw new AvailabilityException("No availability found for date: " + formattedDate);
    }
    Availability availability = availabilityOpt.get();
    if (time.contains("morning")) {
      availability.setMorning(false);
    } else if (time.contains("afternoon")) {
      availability.setAfternoon(false);
    } else if (time.contains("night")) {
      availability.setNight(false);
    }
    if (!availability.isMorning() && !availability.isAfternoon() && !availability.isNight()) {
      availability.setAvailable(false);
    }
    availabilityRepo.save(availability);
  }

  public void isAvailableAt(Date appointmentDate) throws AvailabilityException {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String formattedDate = sdf.format(appointmentDate);
    Optional<Availability> availabilityObj = availabilityRepo.findById(formattedDate);
    if(availabilityObj.isEmpty() || !availabilityObj.get().isAvailable()) throw new AvailabilityException("This date is no longer available");
  }

  public List<Availability> getAllAvailability() {
    Date now = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(now);
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    Date yesterday = calendar.getTime();
    return availabilityRepo.findByIsAvailableTrueAndExpirationDateAfter(yesterday);
  }

}

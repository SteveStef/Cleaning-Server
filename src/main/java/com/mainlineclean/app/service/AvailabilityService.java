package com.mainlineclean.app.service;

import com.mainlineclean.app.model.Time;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

  public void updateAvailability(Appointment app, boolean isAvailable) throws AvailabilityException {
    Date date = app.getAppointmentDate();
    Time time = app.getTime();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String formattedDate = sdf.format(date);
    Optional<Availability> availabilityOpt = availabilityRepo.findById(formattedDate);
    if (!isAvailable && availabilityOpt.isEmpty()) { // if we are making less availability and already found no availability
      log.warn("No availability found for date: {}, we cannot create less availability if there is already non", formattedDate);
      throw new AvailabilityException("No availability found for date: " + formattedDate);
    }
    Availability availability = availabilityOpt.get();
    if (time == Time.MORNING) {
      availability.setMorning(isAvailable);
    } else if (time == Time.AFTERNOON) {
      availability.setAfternoon(isAvailable);
    } else if (time == Time.NIGHT) {
      availability.setNight(isAvailable);
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
    if(availabilityObj.isEmpty() || !availabilityObj.get().isAvailable()) {
      log.warn("Date {} is no longer available", formattedDate);
      throw new AvailabilityException("This date is no longer available");
    }
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

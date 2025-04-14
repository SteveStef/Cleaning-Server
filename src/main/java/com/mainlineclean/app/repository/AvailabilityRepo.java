
package com.mainlineclean.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainlineclean.app.entity.Availability;

import java.util.List;
import java.util.Date;

@Repository
public interface AvailabilityRepo extends JpaRepository<Availability, String> {
  List<Availability> findByIsAvailableTrueAndExpirationDateAfter(Date now);
}

package com.mainlineclean.app.repository;

import com.mainlineclean.app.entity.AdminDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminDetailsRepo extends JpaRepository<AdminDetails, Long> {}

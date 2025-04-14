
package com.mainlineclean.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainlineclean.app.entity.Review;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

}

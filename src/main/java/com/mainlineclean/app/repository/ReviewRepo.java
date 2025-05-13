
package com.mainlineclean.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainlineclean.app.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {
    List<Review> findByStarsGreaterThanEqual(int stars);
    List<Review> findTop5ByStarsGreaterThanEqualOrderByStarsDesc(int stars);
}

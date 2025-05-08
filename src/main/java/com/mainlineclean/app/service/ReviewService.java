package com.mainlineclean.app.service;

import com.mainlineclean.app.entity.Review;
import com.mainlineclean.app.repository.ReviewRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ReviewService {

  private final ReviewRepo reviewRepo;

  public ReviewService(ReviewRepo reviewRepo) {
    this.reviewRepo = reviewRepo;
  }

  public Review createReview(Review review) {
    return reviewRepo.save(review);
  }

  public List<Review> getAllReviews() {
    return reviewRepo.findByStarsGreaterThanEqual(3);
  }

}

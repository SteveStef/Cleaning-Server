package com.mainlineclean.app.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mainlineclean.app.entity.PaymentIntent;

@Repository
public interface PaymentIntentRepo extends JpaRepository<PaymentIntent, Long> {
  PaymentIntent findByOrderId(String orderId);
}

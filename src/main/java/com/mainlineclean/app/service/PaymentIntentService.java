package com.mainlineclean.app.service;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.State;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.repository.PaymentIntentRepo;
import com.mainlineclean.app.utils.Finances;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.mainlineclean.app.exception.PaymentException;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class PaymentIntentService {

  private final PaymentIntentRepo paymentIntentRepo;
  private final AdminDetailsService adminDetailsService;
  private final AppointmentService appointmentService;

  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Value("${paypal.client-id}")
  private String clientId;

  @Value("${paypal.client-secret}")
  private String clientSecret;

  @Value("${paypal.auth-token-url}")
  private String PAYPAL_TOKEN_URL;

  @Value("${paypal.checkout-base-url}")
  private String PAYPAL_CHECKOUT_URL;

  @Value("${paypal.payment-base-url}")
  private String PAYPAL_PAYMENT_URL;

  @Value("${application.fee}")
  private String APPLICATION_FEE;

  @Value("${paypal.payout-email}")
  private String STEVE_PAYPAL_EMAIL;

  @Value("${paypal.payout-url}")
  private String PAYPAL_PAYOUT_URL;

  public PaymentIntentService (PaymentIntentRepo paymentIntentRepo, AdminDetailsService adminDetailsService, AppointmentService appointmentService) {
    this.paymentIntentRepo = paymentIntentRepo;
    this.adminDetailsService = adminDetailsService;
    this.appointmentService = appointmentService;
  }

  public void refundPayment(Appointment appointment, BigDecimal refundAmount) {
    log.info("Refunding payment for appointment {} for a refund of {}", appointment.getId(), refundAmount.toPlainString());

    String accessToken = getAccessToken();
    String refundUrl = PAYPAL_PAYMENT_URL + appointment.getCaptureId() + "/refund";
    Map<String, Object> payloadMap = new HashMap<>();
    Map<String, String> amountNode = new HashMap<>();

    amountNode.put("value", refundAmount.toPlainString());
    amountNode.put("currency_code", "USD");
    payloadMap.put("amount", amountNode);

    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(payloadMap);
    } catch (Exception e) {
      throw new PaymentException("Failed to serialize refund payload", e);
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(refundUrl))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .header("PayPal-Request-Id", UUID.randomUUID().toString())
            .header("Prefer", "return=representation")
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() / 100 != 2) {
        throw new PaymentException("HTTP error: " + response.statusCode() + " - " + response.body());
      }

      String refundedText = objectMapper
              .readTree(response.body())
              .path("amount")
              .path("value")
              .asText();

      BigDecimal refundedValue = new BigDecimal(refundedText);

      // update the appointment will the new amount that we charged the user
      appointment.setChargedAmount(appointment.getChargedAmount().subtract(refundedValue).setScale(2,RoundingMode.HALF_EVEN));
      appointment.setGrossAmount(appointment.getGrossAmount().subtract(refundedValue).setScale(2,RoundingMode.HALF_EVEN));

      appointmentService.updateStatus(appointment, Status.CANCELED);
      log.info("Payment for appointment {} refunded successfully", appointment.getId());
    } catch (Exception e) {
      throw new PaymentException("Error refunding payment: " + e.getMessage(), e);
    }

  }

  public void customerCancelPayment(Appointment appointment, BigDecimal percentBack) throws PaymentException {
    BigDecimal chargedAmount = appointment.getChargedAmount();
    BigDecimal refundAmount = chargedAmount.multiply(percentBack).setScale(2, RoundingMode.HALF_EVEN);
    refundPayment(appointment, refundAmount);
  }

  public PaymentIntent createOrder(ServiceType serviceType, int squareFeet, State state) throws PaymentException, EnumConstantNotPresentException {

    PaymentIntent pi = new PaymentIntent();
    AdminDetails details = adminDetailsService.getAdminDetails();

    BigDecimal ratePerSquareFeet = switch (serviceType) {
      case DEEP               -> details.getDeepCleanPrice();
      case MOVE_IN_OUT        -> details.getMoveInOutPrice();
      case FIRE               -> details.getFirePrice();
      case REGULAR            -> details.getRegularPrice();
      case COMMERCIAL         -> details.getCommercialPrice();
      case CONSTRUCTION       -> details.getConstructionPrice();
      case DECEASED           -> details.getDeceasedPrice();
      case ENVIRONMENT        -> details.getEnvironmentPrice();
      case EXPLOSIVE_RESIDUE  -> details.getExplosiveResiduePrice();
      case HAZMAT             -> details.getHazmatPrice();
      case MOLD               -> details.getMoldPrice();
      case WATER              -> details.getWaterPrice();
      default -> throw new IllegalArgumentException(
              "Unknown service type: " + serviceType);
    };

    BigDecimal area = BigDecimal.valueOf(squareFeet);
    BigDecimal baseCost = ratePerSquareFeet.multiply(area);

    BigDecimal appFee = new BigDecimal(APPLICATION_FEE);
    BigDecimal subtotal = baseCost.add(appFee);

    BigDecimal taxFactor = Finances.taxMap.get(state); // 1.06 or ...
    BigDecimal totalCost = subtotal.multiply(taxFactor).setScale(2, RoundingMode.HALF_EVEN);

    pi.setPrice(totalCost);

    log.info("Created payment intent for service type {} and square feet {} with total cost of {}", serviceType, squareFeet, totalCost.toPlainString());
    String orderId = createOrder(pi);
    pi.setOrderId(orderId);

    return paymentIntentRepo.save(pi);
  }

  public PaymentIntent findPaymentIntentByOrderId(String orderId) {
    return paymentIntentRepo.findByOrderId(orderId);
  }

  public String capturePaymentIntent(PaymentIntent pi, String accessToken) throws PaymentException {
    String captured = captureOrder(pi, accessToken);
    paymentIntentRepo.deleteById(pi.getId());
    return captured;
  }

  public void sendPayout(String accessToken) {
    String amount = APPLICATION_FEE;
    String currency = "USD";
    String batchId = UUID.randomUUID().toString();

    String json = "{"
          + "\"sender_batch_header\":{"
            + "\"sender_batch_id\":\"" + batchId + "\","
            + "\"email_subject\":\"You’ve got a payout!\""
          + "},"
          + "\"items\":[{"
            + "\"recipient_type\":\"EMAIL\","
            + "\"receiver\":\"" + STEVE_PAYPAL_EMAIL + "\","
            + "\"amount\":{"
              + "\"value\":\"" + amount + "\","
              + "\"currency\":\"" + currency + "\""
            + "},"
            + "\"note\":\"Here’s your share!\","
            + "\"sender_item_id\":\"" + batchId + "_item\""
          + "}]"
        + "}";

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(PAYPAL_PAYOUT_URL))
            .header("Content-Type", "application/json")
            .header("Authorization",  "Bearer " + accessToken)
            .header("PayPal-Request-Id", batchId)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
    try {
      HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() / 100 != 2) {
        throw new PaymentException("HTTP error: " + resp.statusCode() + " - " + resp.body());
      }
      log.info("Payout sent successfully to {}", STEVE_PAYPAL_EMAIL);
    } catch (Exception e) {
      throw new PaymentException("Error sending payout: " + e.getMessage());
    }
  }

  private String captureOrder(PaymentIntent intent, String accessToken) throws PaymentException {
    String url = PAYPAL_CHECKOUT_URL + intent.getOrderId() + "/capture";
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .header("PayPal-Request-Id", intent.getRequestId())
            .POST(HttpRequest.BodyPublishers.ofString("{}"))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if(response.statusCode() / 100 != 2) {
        throw new PaymentException("HTTP error: " + response.statusCode() + " - " + response.body());
      }
      log.info("Captured order {} successfully", intent.getOrderId());
      return response.body();
    } catch(Exception e) {
      throw new PaymentException("Error capturing order " + e.getMessage(), e);
    }
  }

  public String getAccessToken() throws PaymentException {
    String auth = clientId + ":" + clientSecret;
    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(PAYPAL_TOKEN_URL))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Authorization", "Basic " + encodedAuth)
            .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if(response.statusCode() / 100 != 2) throw new PaymentException("HTTP error: " + response.statusCode() + " - " + response.body());
      log.info("Got access token successfully");
      return objectMapper.readValue(response.body(), Map.class).get("access_token").toString();
    } catch(Exception e) {
      throw new PaymentException("Error getting access token" + e.getMessage());
    }
  }

  public String createOrder(PaymentIntent intent) throws PaymentException {
    String accessToken = getAccessToken();

    Map<String, Object> payloadMap = new HashMap<>();
    payloadMap.put("intent", "CAPTURE");

    Map<String, Object> amount = new HashMap<>();
    amount.put("currency_code", "USD");
    amount.put("value", intent.getPrice());

    Map<String, Object> purchaseUnit = new HashMap<>();
    purchaseUnit.put("amount", amount);

    List<Map<String, Object>> purchaseUnits = new ArrayList<>();
    purchaseUnits.add(purchaseUnit);
    payloadMap.put("purchase_units", purchaseUnits);

    String jsonPayload;
    try {
      jsonPayload = objectMapper.writeValueAsString(payloadMap);
    } catch (Exception e) {
      throw new PaymentException("Failed to serialize payload", e);
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(PAYPAL_CHECKOUT_URL))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + accessToken)
            .header("PayPal-Request-Id", intent.getRequestId())
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if(response.statusCode() / 100 != 2) throw new PaymentException("HTTP error: " + response.statusCode() + " - " + response.body());
      Map<String, Object> resMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
      Object orderIdObj = resMap.get("id");
      if(orderIdObj == null) throw new PaymentException("Order ID response came back differently than expected");
      return (String) orderIdObj;
    } catch(Exception e) {
      throw new PaymentException("HTTP error, cannot create order", e);
    }
  }
}

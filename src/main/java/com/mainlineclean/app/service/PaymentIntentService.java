package com.mainlineclean.app.service;
import com.fasterxml.jackson.core.type.TypeReference;

import com.mainlineclean.app.entity.AdminDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.entity.PaymentIntent;
import com.mainlineclean.app.model.ServiceType;
import com.mainlineclean.app.model.Status;
import com.mainlineclean.app.repository.PaymentIntentRepo;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.mainlineclean.app.exception.PaymentException;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
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

  // Constructor
  public PaymentIntentService (PaymentIntentRepo paymentIntentRepo, AdminDetailsService adminDetailsService, AppointmentService appointmentService) {
    this.paymentIntentRepo = paymentIntentRepo;
    this.adminDetailsService = adminDetailsService;
    this.appointmentService = appointmentService;
  }

  public void cancelPayment(Appointment appointmentInput) throws PaymentException {
    Appointment appointment = appointmentService.findById(appointmentInput.getId());
    String accessToken = getAccessToken();
    String refundUrl = PAYPAL_PAYMENT_URL + appointment.getCaptureId() + "/refund";

    Map<String, Object> payloadMap = new HashMap<>();
    Map<String, String> amount = new HashMap<>();
    amount.put("value", appointment.getNetAmount().split(" ")[0]); // this is in the form "122.45 USD"
    amount.put("currency_code", "USD");
    payloadMap.put("amount", amount);

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
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .build();
    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() / 100 != 2) {
        throw new PaymentException("HTTP error: " + response.statusCode() + " - " + response.body());
      }
      appointmentService.updateStatus(appointment, Status.CANCELED);
    } catch (Exception e) {
      throw new PaymentException("Error refunding payment: " + e.getMessage(), e);
    }
  }

  public PaymentIntent createOrder(ServiceType serviceType) throws PaymentException, EnumConstantNotPresentException {
    PaymentIntent pi = new PaymentIntent();
    AdminDetails details = adminDetailsService.getAdminDetails();

    String price = switch (serviceType) {
      case DEEP               -> details.getDeepCleanPrice();
      case MOVE_IN_OUT        -> details.getMoveInOutPrice();
      case FIRE               -> details.getFirePrice();
      case REGULAR            -> details.getRegularPrice();
      case COMMERCIAL         -> details.getCommercialPrice();
      case CONSTRUCTION       -> details.getConstructionPrice();
      case DECEASED           -> details.getDeceasedPrice();
      case ENVIRONMENT        -> details.getEnvironmentPrice();
      case EXPLOSIVE_RESIDUE  -> details.getExplosiveResidue();
      case HAZMAT             -> details.getHazmat();
      case MOLD               -> details.getMoldPrice();
      case WATER              -> details.getWaterPrice();
      default -> throw new IllegalArgumentException(
              "Unknown service type: " + serviceType);
    };

    // add sales tax
    double priceVal = Double.parseDouble(price);
    double totalPrice = priceVal * 1.06;
    totalPrice = Math.round(totalPrice * 100.0) / 100.0;

    pi.setPrice(Double.toString(totalPrice));
    String orderId = createOrder(pi);
    pi.setOrderId(orderId);
    return paymentIntentRepo.save(pi);
  }

  public PaymentIntent findPaymentIntentByOrderId(String orderId) {
    return paymentIntentRepo.findByOrderId(orderId);
  }

  public String capturePaymentIntent(PaymentIntent pi) throws PaymentException {
    String captured = captureOrder(pi);
    paymentIntentRepo.deleteById(pi.getId());
    return captured;
  }

  private String captureOrder(PaymentIntent intent) throws PaymentException {
    String accessToken = getAccessToken();
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
      return response.body();
    } catch(Exception e) {
      throw new PaymentException("Error capturing order " + e.getMessage(), e);
    }
  }

  private String getAccessToken() throws PaymentException {
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
      return objectMapper.readValue(response.body(), Map.class).get("access_token").toString();
    } catch(Exception e) {
      throw new PaymentException("Error getting access token", e);
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

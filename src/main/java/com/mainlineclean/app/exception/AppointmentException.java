package com.mainlineclean.app.exception;

public class AppointmentException extends RuntimeException {
  public AppointmentException(String message) {
    super(message);
  }
  public AppointmentException(String message, Throwable cause) {
    super(message, cause);
  }
}

package com.mainlineclean.app.utils;

import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.service.AppointmentService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Finances {
    private final AppointmentService appointmentService;
    public Finances(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public RevenueDetails financeDetails() {
        double gross = 0, baseAmountSum = 0, paypalFee = 0;
        List<Appointment> appointments = appointmentService.getAllSuccessfulAppointments();
        for (Appointment appointment : appointments) {
            baseAmountSum += Double.parseDouble(appointment.getChargedAmount().split(" ")[0]) / 1.06;
            gross += Double.parseDouble(appointment.getNetAmount().split(" ")[0]);
            paypalFee += Double.parseDouble(appointment.getPaypalFee().split(" ")[0]);
        }

        double salesTax = 0.06 * baseAmountSum;
        double profit = gross - salesTax;

        gross    = Math.round(gross    * 100) / 100.0;
        salesTax = Math.round(salesTax * 100) / 100.0;
        profit   = Math.round(profit   * 100) / 100.0;
        paypalFee= Math.round(paypalFee* 100) / 100.0;

        return new RevenueDetails(profit, gross, salesTax, paypalFee);
    }
}
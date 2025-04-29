package com.mainlineclean.app.utils;

import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.service.AppointmentService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class Finances {
    public record RevenueEntry(String month, double revenue){
        @Override
        public String toString() {
            return "RevenueEntry{" +
                    "month='" + month + '\'' +
                    ", revenue=" + revenue +
                    '}';
        }
    }
    public record YearlyEntry(int year, double revenue, double profit, double salesTax, double paypalFee){
        @Override
        public String toString() {
            return "YearlyEntry{" +
                    "year=" + year +
                    ", revenue=" + revenue +
                    ", profit=" + profit +
                    ", salesTax=" + salesTax +
                    ", paypalFee=" + paypalFee +
                    '}';
        }
    }

    private final AppointmentService appointmentService;
    public Finances(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public RevenueDetails financeDetails() {
        double gross = 0, baseAmountSum = 0, paypalFee = 0;
        ArrayList<RevenueEntry> monthlyRevenue = new ArrayList<>(); // this is only this year
        ArrayList<YearlyEntry> yearlyRevenue = new ArrayList<>();
        int thisYear = LocalDate.now(ZoneId.systemDefault()).getYear();
        List<Appointment> appointments = appointmentService.getAllAppointments();
        Map<String, Double> revMap = new HashMap<>();
        Map<Integer, double[]> yearlyRevMap = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        for (Appointment appointment : appointments) {
            ZonedDateTime zdt = appointment.getCreatedAt().toInstant().atZone(ZoneId.systemDefault());
            LocalDate ld = zdt.toLocalDate();
            String month = zdt.format(fmt);
            int year = ld.getYear();

            double paypalFeeForThisAppointment = Double.parseDouble(appointment.getPaypalFee().split(" ")[0]);
            double baseForThisAppointment = Double.parseDouble(appointment.getChargedAmount().split(" ")[0]) / 1.06;
            double applicationFee = Double.parseDouble(appointment.getApplicationFee()); // 9.99
            double grossedForThisAppointment = Double.parseDouble(appointment.getNetAmount().split(" ")[0]) - applicationFee;

            if(year == thisYear) {
                double prev = revMap.getOrDefault(month, 0.0);
                revMap.put(month, prev + grossedForThisAppointment);
            }

            double[] prev = yearlyRevMap.getOrDefault(year, new double[]{0, 0, 0});
            yearlyRevMap.put(year, new double[]{prev[0] + grossedForThisAppointment, prev[1] + paypalFeeForThisAppointment, prev[2] + baseForThisAppointment});

            gross += grossedForThisAppointment;
            paypalFee += paypalFeeForThisAppointment;
            baseAmountSum += baseForThisAppointment;
        }

        for (String month : revMap.keySet()) {
            double revenue = round(revMap.get(month));
            monthlyRevenue.add(new RevenueEntry(month, revenue));
        }

        for (int year : yearlyRevMap.keySet()) {
            double[] stat      = yearlyRevMap.get(year);
            double grossRaw    = stat[0];
            double feeRaw      = stat[1];
            double baseRaw     = stat[2];

            double taxRaw      = 0.06 * baseRaw;
            double profitRaw   = grossRaw - taxRaw;

            double grss       = round(grossRaw);
            double fee         = round(feeRaw);
            double tax         = round(taxRaw);
            double prof = round(profitRaw);

            yearlyRevenue.add(new YearlyEntry(year, grss, prof, tax, fee));
        }

        double salesTax = 0.06 * baseAmountSum;
        double profit = gross - salesTax;

        gross     = round(gross);
        salesTax  = round(salesTax);
        profit    = round(profit);
        paypalFee = round(paypalFee);

        return new RevenueDetails(profit, gross, salesTax, paypalFee, yearlyRevenue, monthlyRevenue);
    }
    private double round(double amount) {
        return Math.round(amount * 100) / 100.0;
    }
}
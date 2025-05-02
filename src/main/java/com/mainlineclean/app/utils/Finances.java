package com.mainlineclean.app.utils;

import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.service.AppointmentService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
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
        BigDecimal grossSum       = BigDecimal.ZERO;
        BigDecimal baseAmountSum  = BigDecimal.ZERO;
        BigDecimal paypalFeeSum   = BigDecimal.ZERO;

        ArrayList<RevenueEntry> monthlyRevenue = new ArrayList<>(); // this is only this year
        ArrayList<YearlyEntry> yearlyRevenue = new ArrayList<>();
        int thisYear = LocalDate.now(ZoneId.systemDefault()).getYear();

        List<Appointment> appointments = appointmentService.getAllAppointments();

        Map<String, BigDecimal> revMap = new HashMap<>();
        Map<Integer, BigDecimal[]> yearlyRevMap = new HashMap<>();

        BigDecimal taxDivisor = BigDecimal.valueOf(1.06);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        for (Appointment appointment : appointments) {
            ZonedDateTime zdt = appointment.getCreatedAt().toInstant().atZone(ZoneId.systemDefault());
            LocalDate ld = zdt.toLocalDate();
            String month = zdt.format(fmt);
            int year = ld.getYear();

            BigDecimal paypalFeeForThisAppointment = appointment.getPaypalFee();
            BigDecimal baseForThisAppointment = appointment.getChargedAmount().divide(taxDivisor, 2, RoundingMode.HALF_EVEN);

            BigDecimal applicationFee = appointment.getApplicationFee(); // 9.99
            BigDecimal grossedForThisAppointment = appointment.getGrossAmount().subtract(applicationFee);

            if(year == thisYear) {
                revMap.merge(month, grossedForThisAppointment, BigDecimal::add);
            }

            BigDecimal[] stats = yearlyRevMap.getOrDefault(year, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
            stats[0] = stats[0].add(grossedForThisAppointment);
            stats[1] = stats[1].add(paypalFeeForThisAppointment);
            stats[2] = stats[2].add(baseForThisAppointment);
            yearlyRevMap.put(year, stats);

            // grand totals
            grossSum      = grossSum.add(grossedForThisAppointment);
            paypalFeeSum  = paypalFeeSum.add(paypalFeeForThisAppointment);
            baseAmountSum = baseAmountSum.add(baseForThisAppointment);
        }

        // build monthlyRevenue DTOs
        for (Map.Entry<String, BigDecimal> e : revMap.entrySet()) {
            BigDecimal val = e.getValue().setScale(2, RoundingMode.HALF_EVEN);
            monthlyRevenue.add(new RevenueEntry(e.getKey(), val.doubleValue()));
        }

        monthlyRevenue.sort(Comparator.comparingInt(e -> Month.from(fmt.parse(e.month())).getValue()));

        for (Map.Entry<Integer, BigDecimal[]> entry : yearlyRevMap.entrySet()) {
            int year        = entry.getKey();
            BigDecimal[] s  = entry.getValue();

            BigDecimal grossRaw = s[0].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal feeRaw   = s[1].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal baseRaw  = s[2].setScale(2, RoundingMode.HALF_EVEN);

            BigDecimal taxRaw = baseRaw.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal profitRaw = grossRaw.subtract(taxRaw).setScale(2, RoundingMode.HALF_EVEN);

            yearlyRevenue.add(new YearlyEntry(
                    year,
                    grossRaw.doubleValue(),
                    profitRaw.doubleValue(),
                    taxRaw.doubleValue(),
                    feeRaw.doubleValue()
            ));
        }

        yearlyRevenue.sort(Comparator.comparingInt(YearlyEntry::year).reversed());

        // compute overall salesTax & profit
        BigDecimal salesTax = baseAmountSum.multiply(BigDecimal.valueOf(0.06)).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal profit   = grossSum.subtract(salesTax).setScale(2, RoundingMode.HALF_EVEN);

        // final doubles for DTO
        double grossD     = grossSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        double salesTaxD  = salesTax.doubleValue();
        double profitD    = profit.doubleValue();
        double paypalFeeD = paypalFeeSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue();

        return new RevenueDetails(
                profitD,
                grossD,
                salesTaxD,
                paypalFeeD,
                yearlyRevenue,
                monthlyRevenue
        );
    }
}
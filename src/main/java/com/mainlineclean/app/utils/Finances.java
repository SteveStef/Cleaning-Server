package com.mainlineclean.app.utils;

import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.model.State;
import com.mainlineclean.app.service.AppointmentService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.AbstractDetectingUrlHandlerMapping;

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

    public record RevenueEntry(String month, double revenue) {
        @Override
        public String toString() {
            return "RevenueEntry{" +
                    "month='" + month + '\'' +
                    ", revenue=" + revenue +
                    '}';
        }
    }

    public record YearlyEntry(int year, double revenue, double profit, double salesTax, double paypalFee, double applicationFee) {
        @Override
        public String toString() {
            return "YearlyEntry{" +
                    "year=" + year +
                    ", revenue=" + revenue +
                    ", profit=" + profit +
                    ", salesTax=" + salesTax +
                    ", paypalFee=" + paypalFee +
                    ", applicationFee=" + applicationFee +
                    '}';
        }
    }

    public final static Map<State, BigDecimal> taxMap = new HashMap<>();
    static {
        taxMap.put(State.PA, BigDecimal.valueOf(1.06));
        taxMap.put(State.NJ, BigDecimal.valueOf(1.06625));
        taxMap.put(State.DE, BigDecimal.ONE);
    }

    private final AppointmentService appointmentService;

    public Finances(AppointmentService appointmentService, AbstractDetectingUrlHandlerMapping abstractDetectingUrlHandlerMapping) {
        this.appointmentService = appointmentService;
    }

    public RevenueDetails financeDetails() {
        BigDecimal grossSum       = BigDecimal.ZERO;
        BigDecimal paypalFeeSum   = BigDecimal.ZERO;
        BigDecimal salesTaxSum    = BigDecimal.ZERO;
        BigDecimal profitSum      = BigDecimal.ZERO;

        List<RevenueEntry> monthlyRevenue = new ArrayList<>();
        List<YearlyEntry> yearlyRevenue   = new ArrayList<>();
        int thisYear = LocalDate.now(ZoneId.systemDefault()).getYear();

        List<Appointment> appointments = appointmentService.getAllAppointments();

        // month → grossed revenue
        Map<String, BigDecimal> revMap = new HashMap<>();
        // year → [ gross, paypalFee, salesTax ]
        Map<Integer, BigDecimal[]> yearlyRevMap = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        for (Appointment apt : appointments) {
            ZonedDateTime zdt = apt.getCreatedAt().toInstant().atZone(ZoneId.systemDefault());
            String month = zdt.format(fmt);
            LocalDate ld = zdt.toLocalDate();
            int year = ld.getYear();

            if (year == thisYear) {
                revMap.merge(month, apt.getGrossAmount(), BigDecimal::add);
            }

            // yearly buckets: [gross, paypalFee, salesTax, profit, applicationFee]
            BigDecimal[] stats = yearlyRevMap.computeIfAbsent(
                    year, y -> new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO}
            );
            stats[0] = stats[0].add(apt.getGrossAmount());
            stats[1] = stats[1].add(apt.getPaypalFee());
            stats[2] = stats[2].add(apt.getSalesTax());
            stats[3] = stats[3].add(apt.getProfit());
            stats[4] = stats[4].add(apt.getApplicationFee());

            // accumulate overall
            grossSum     = grossSum.add(apt.getGrossAmount());
            paypalFeeSum = paypalFeeSum.add(apt.getPaypalFee());
            salesTaxSum  = salesTaxSum.add(apt.getSalesTax());
            profitSum    = profitSum.add(apt.getProfit());
        }

        revMap.forEach((m, val) -> {
            BigDecimal rounded = val.setScale(2, RoundingMode.HALF_EVEN);
            monthlyRevenue.add(new RevenueEntry(m, rounded.doubleValue()));
        });

        monthlyRevenue.sort(Comparator.comparingInt(e ->
                Month.from(fmt.parse(e.month())).getValue()
        ));

        for (Map.Entry<Integer, BigDecimal[]> entry : yearlyRevMap.entrySet()) {
            int year = entry.getKey();
            BigDecimal[] s = entry.getValue();

            BigDecimal grossRaw = s[0].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal feeRaw   = s[1].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal taxRaw   = s[2].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal profitRaw= s[3].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal applicationFeeRaw = s[4].setScale(2, RoundingMode.HALF_EVEN);

            yearlyRevenue.add(new YearlyEntry(
                    year,
                    grossRaw.doubleValue(),
                    profitRaw.doubleValue(),
                    taxRaw.doubleValue(),
                    feeRaw.doubleValue(),
                    applicationFeeRaw.doubleValue()
            ));
        }

        yearlyRevenue.sort(Comparator.comparingInt(YearlyEntry::year).reversed());
        return new RevenueDetails(
                profitSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                grossSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                salesTaxSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                paypalFeeSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                yearlyRevenue,
                monthlyRevenue
        );
    }
}

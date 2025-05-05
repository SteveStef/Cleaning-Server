package com.mainlineclean.app.utils;

import com.mainlineclean.app.dto.RevenueDetails;
import com.mainlineclean.app.entity.Appointment;
import com.mainlineclean.app.model.State;
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

    public record RevenueEntry(String month, double revenue) {
        @Override
        public String toString() {
            return "RevenueEntry{" +
                    "month='" + month + '\'' +
                    ", revenue=" + revenue +
                    '}';
        }
    }

    public record YearlyEntry(int year, double revenue, double profit, double salesTax, double paypalFee) {
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

    public final static Map<State, BigDecimal> taxMap = new HashMap<>();
    static {
        taxMap.put(State.PA, BigDecimal.valueOf(1.06));
        taxMap.put(State.NJ, BigDecimal.valueOf(1.06625));
        taxMap.put(State.DE, BigDecimal.ONE);
    }

    private final AppointmentService appointmentService;

    public Finances(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public RevenueDetails financeDetails() {
        // high-precision accumulators
        BigDecimal grossSum       = BigDecimal.ZERO;
        BigDecimal paypalFeeSum   = BigDecimal.ZERO;
        BigDecimal salesTaxSum    = BigDecimal.ZERO;

        List<RevenueEntry> monthlyRevenue = new ArrayList<>();
        List<YearlyEntry> yearlyRevenue   = new ArrayList<>();
        int thisYear = LocalDate.now(ZoneId.systemDefault()).getYear();

        List<Appointment> appointments = appointmentService.getAllAppointments();

        // month → grossed revenue
        Map<String, BigDecimal> revMap = new HashMap<>();
        // year → [ gross, paypalFee, salesTax ]
        Map<Integer, BigDecimal[]> yearlyRevMap = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

        for (Appointment appt : appointments) {
            BigDecimal taxDivisor = taxMap.getOrDefault(appt.getState(), BigDecimal.ONE);

            ZonedDateTime zdt = appt.getCreatedAt()
                    .toInstant()
                    .atZone(ZoneId.systemDefault());
            String month = zdt.format(fmt);
            LocalDate ld = zdt.toLocalDate();
            int year = ld.getYear();

            BigDecimal chargedAmount = appt.getChargedAmount();
            // divide with extra precision, round only at final reporting
            BigDecimal baseAmount = chargedAmount.divide(taxDivisor, 10, RoundingMode.HALF_EVEN);
            BigDecimal taxForThisAppointment = chargedAmount.subtract(baseAmount);

            BigDecimal paypalFeeForThisAppointment = appt.getPaypalFee();
            BigDecimal applicationFee              = appt.getApplicationFee();
            BigDecimal grossedForThisAppointment   = appt.getGrossAmount().subtract(applicationFee);

            if (year == thisYear) {
                revMap.merge(month, grossedForThisAppointment, BigDecimal::add);
            }

            // yearly buckets: [ gross, paypalFee, salesTax ]
            BigDecimal[] stats = yearlyRevMap.computeIfAbsent(year,
                    y -> new BigDecimal[]{ BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO });
            stats[0] = stats[0].add(grossedForThisAppointment);
            stats[1] = stats[1].add(paypalFeeForThisAppointment);
            stats[2] = stats[2].add(taxForThisAppointment);

            // accumulate overall
            grossSum     = grossSum.add(grossedForThisAppointment);
            paypalFeeSum = paypalFeeSum.add(paypalFeeForThisAppointment);
            salesTaxSum  = salesTaxSum.add(taxForThisAppointment);
        }

        // build monthly revenue list (round at presentation)
        revMap.forEach((m, val) -> {
            BigDecimal rounded = val.setScale(2, RoundingMode.HALF_EVEN);
            monthlyRevenue.add(new RevenueEntry(m, rounded.doubleValue()));
        });
        monthlyRevenue.sort(Comparator.comparingInt(e ->
                Month.from(fmt.parse(e.month())).getValue()
        ));

        // build yearly revenue list (round at presentation)
        for (Map.Entry<Integer, BigDecimal[]> entry : yearlyRevMap.entrySet()) {
            int year = entry.getKey();
            BigDecimal[] s = entry.getValue();

            BigDecimal grossRaw = s[0].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal feeRaw   = s[1].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal taxRaw   = s[2].setScale(2, RoundingMode.HALF_EVEN);
            BigDecimal profitRaw= grossRaw.subtract(taxRaw).setScale(2, RoundingMode.HALF_EVEN);

            yearlyRevenue.add(new YearlyEntry(
                    year,
                    grossRaw.doubleValue(),
                    profitRaw.doubleValue(),
                    taxRaw.doubleValue(),
                    feeRaw.doubleValue()
            ));
        }
        yearlyRevenue.sort(Comparator.comparingInt(YearlyEntry::year).reversed());

        // overall profit, rounded once
        BigDecimal totalProfit = grossSum
                .subtract(salesTaxSum)
                .setScale(2, RoundingMode.HALF_EVEN);

        return new RevenueDetails(
                totalProfit.doubleValue(),
                grossSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                salesTaxSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                paypalFeeSum.setScale(2, RoundingMode.HALF_EVEN).doubleValue(),
                yearlyRevenue,
                monthlyRevenue
        );
    }
}

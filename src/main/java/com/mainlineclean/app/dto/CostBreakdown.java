package com.mainlineclean.app.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CostBreakdown {

    @JsonProperty("gross_amount")
    private Amount grossAmount;

    @JsonProperty("paypal_fee")
    private Amount paypalFee;

    @JsonProperty("net_amount")
    private Amount netAmount;

    public Amount getGrossAmount() {
        return grossAmount;
    }
    public void setGrossAmount(Amount grossAmount) {
        this.grossAmount = grossAmount;
    }
    public Amount getPaypalFee() {
        return paypalFee;
    }
    public void setPaypalFee(Amount paypalFee) {
        this.paypalFee = paypalFee;
    }
    public Amount getNetAmount() {
        return netAmount;
    }
    public void setNetAmount(Amount netAmount) {
        this.netAmount = netAmount;
    }

    @Override
    public String toString() {
        return "CostBreakdown{" +
                "grossAmount=" + grossAmount +
                ", paypalFee=" + paypalFee +
                ", netAmount=" + netAmount +
                '}';
    }
}

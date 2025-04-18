package com.mainlineclean.app.dto;

public class RevenueDetails {
    private double profit;
    private double gross;
    private double paypalFee;
    private double salesTax;

    public RevenueDetails(double profit, double gross, double salesTax, double paypalFee) {
        this.profit = profit;
        this.gross = gross;
        this.paypalFee = paypalFee;
        this.salesTax = salesTax;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }

    public double getGross() {
        return gross;
    }

    public void setGross(double gross) {
        this.gross = gross;
    }

    public double getPaypalFee() {
        return paypalFee;
    }

    public void setPaypalFee(double paypalFee) {
        this.paypalFee = paypalFee;
    }

    public double getSalesTax() {
        return salesTax;
    }

    public void setSalesTax(double salesTax) {
        this.salesTax = salesTax;
    }
}

package com.mainlineclean.app.dto;

import com.mainlineclean.app.utils.Finances;

import java.util.ArrayList;

public class RevenueDetails {

    private double profit;
    private double gross;
    private double paypalFee;
    private double salesTax;
    private ArrayList<Finances.YearlyEntry> yearlyRevenue;
    private ArrayList<Finances.RevenueEntry> monthlyRevenue;

    public RevenueDetails(double profit, double gross, double salesTax, double paypalFee, ArrayList<Finances.YearlyEntry> yearlyRevenue, ArrayList<Finances.RevenueEntry> monthlyRevenue) {
        this.profit = profit;
        this.gross = gross;
        this.paypalFee = paypalFee;
        this.salesTax = salesTax;
        this.yearlyRevenue = yearlyRevenue;
        this.monthlyRevenue = monthlyRevenue;
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

    public ArrayList<Finances.YearlyEntry> getYearlyRevenue() {
        return yearlyRevenue;
    }

    public void setYearlyRevenue(ArrayList<Finances.YearlyEntry> yearlyRevenue) {
        this.yearlyRevenue = yearlyRevenue;
    }

    public ArrayList<Finances.RevenueEntry> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(ArrayList<Finances.RevenueEntry> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    @Override
    public String toString() {
        return "RevenueDetails{" +
                "profit=" + profit +
                ", gross=" + gross +
                ", paypalFee=" + paypalFee +
                ", salesTax=" + salesTax +
                ", yearlyRevenue=" + yearlyRevenue +
                ", monthlyRevenue=" + monthlyRevenue +
                '}';
    }
}

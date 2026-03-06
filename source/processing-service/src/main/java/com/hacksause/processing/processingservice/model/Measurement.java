package com.hacksause.processing.processingservice.model;

public class Measurement {
    private String metric;
    private double value;
    private String unit;

    // Getters and setters
    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}

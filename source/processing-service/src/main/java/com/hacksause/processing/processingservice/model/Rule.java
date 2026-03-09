package com.hacksause.processing.processingservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sensorId;
    private String metric;
    private String operator;
    private double threshold;
    private String actuatorName;
    private String actuatorState;
    private boolean enabled = true;

    public Rule() {}

    public Rule(String sensorId, String metric, String operator, double threshold, String actuatorName, String actuatorState) {
        this.sensorId = sensorId;
        this.metric = metric;
        this.operator = operator;
        this.threshold = threshold;
        this.actuatorName = actuatorName;
        this.actuatorState = actuatorState;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getActuatorName() { return actuatorName; }
    public void setActuatorName(String actuatorName) { this.actuatorName = actuatorName; }

    public String getActuatorState() { return actuatorState; }
    public void setActuatorState(String actuatorState) { this.actuatorState = actuatorState; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

}

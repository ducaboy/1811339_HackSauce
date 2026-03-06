package com.hacksause.processing.processingservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SensorEvent {

    @JsonProperty("sensor_id")
    private String sensorId;

    @JsonProperty("captured_at")
    private String capturedAt;

    private String status;

    private List<Measurement> measurements;

    public String getSensorId() {return sensorId; }
    public void setSensorId(String sensorId) {this.sensorId = sensorId;}

    public String getCapturedAt() { return capturedAt; }
    public void setCapturedAt(String capturedAt) { this.capturedAt = capturedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Measurement> getMeasurements() { return measurements; }
    public void setMeasurements(List<Measurement> measurements) { this.measurements = measurements; }

}

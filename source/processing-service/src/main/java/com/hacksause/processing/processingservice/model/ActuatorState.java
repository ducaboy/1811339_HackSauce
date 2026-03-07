package com.hacksause.processing.processingservice.model;

import java.time.Instant;

public class ActuatorState {

    private String name;
    private String state;
    private String lastUpdate;

    public ActuatorState(String name, String state) {
        this.name = name;
        this.state = state;
        this.lastUpdate = Instant.now().toString();
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }
}

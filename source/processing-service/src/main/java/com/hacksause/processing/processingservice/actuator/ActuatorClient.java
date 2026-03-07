package com.hacksause.processing.processingservice.actuator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ActuatorClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${simulator.base-url}")
    private String baseUrl;

    public void trigger(String actuatorName, String state) {
        String url = baseUrl + "/api/actuators/" + actuatorName;
        Map<String, String> body = Map.of("state", state);
        try {
            restTemplate.postForObject(url, body, Map.class);
            System.out.println("Actuator triggered: " + actuatorName + " → " + state);
        } catch (Exception e) {
            System.err.println("Failed to trigger actuator: " + e.getMessage());
        }
    }

}

package com.hacksause.processing.processingservice.actuator;

import com.hacksause.processing.processingservice.model.ActuatorState;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActuatorClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${simulator.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        String[] actuators = {"cooling_fan", "entrance_humidifier", "hall_ventilation", "habitat_heater"};
        for (String actuator : actuators) {
            actuatorCache.put(actuator, new ActuatorState(actuator, "OFF"));
        }
    }
    private final Map<String, ActuatorState> actuatorCache = new ConcurrentHashMap<>();

    public void trigger(String actuatorName, String state) {
        String url = baseUrl + "/api/actuators/" + actuatorName;
        Map<String, String> body = Map.of("state", state);
        try {
            restTemplate.postForObject(url, body, Map.class);
            actuatorCache.put(actuatorName, new ActuatorState(actuatorName, state));
            System.out.println("Actuator triggered: " + actuatorName + " → " + state);
        } catch (Exception e) {
            System.err.println("Failed to trigger actuator: " + e.getMessage());
        }
    }

    public Collection<ActuatorState> getAllStates() {
        return actuatorCache.values();
    }
}

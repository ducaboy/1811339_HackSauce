package com.hacksause.processing.processingservice.consumer;

import com.hacksause.processing.processingservice.model.SensorEvent;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SensorConsumer {

    ObjectMapper objectMapper = new ObjectMapper();

    @JmsListener(destination = "sensor.queue")
    public void onMessage(String message){
        try {
            SensorEvent sensorEvent = objectMapper.readValue(message, SensorEvent.class);
            System.out.println("Message received from queue:");
            System.out.println(message);
            System.out.println(sensorEvent.getSensorId());
            System.out.println(sensorEvent.getCapturedAt());
            System.out.println(sensorEvent.getStatus());
            System.out.println(sensorEvent.getMeasurements().getFirst().getMetric());
            System.out.println(sensorEvent.getMeasurements().getFirst().getUnit());
            System.out.println(sensorEvent.getMeasurements().getFirst().getValue());
        } catch (Exception e){
            System.err.println("Failed to parse message:" + e.getMessage());
        }

    }
}

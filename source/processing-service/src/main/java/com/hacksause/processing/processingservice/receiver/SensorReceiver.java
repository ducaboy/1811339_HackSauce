package com.hacksause.processing.processingservice.receiver;

import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class SensorReceiver {

    @JmsListener(destination = "sensors.events")
    public void receiveMessage(JacksonProperties.Json message){

        System.out.println("Sensor Events updated");
    }
}

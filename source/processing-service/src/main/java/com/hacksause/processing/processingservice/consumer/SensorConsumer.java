package com.hacksause.processing.processingservice.consumer;

import com.hacksause.processing.processingservice.actuator.ActuatorClient;
import com.hacksause.processing.processingservice.cache.SensorCache;
import com.hacksause.processing.processingservice.engine.RuleEngine;
import com.hacksause.processing.processingservice.model.SensorEvent;
import com.hacksause.processing.processingservice.sse.SseEmitterService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SensorConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SensorCache sensorCache;
    private final RuleEngine ruleEngine;
    private final SseEmitterService sseEmitterService;

    public SensorConsumer(SensorCache sensorCache, RuleEngine ruleEngine,
                          SseEmitterService sseEmitterService) {
        this.sensorCache = sensorCache;
        this.ruleEngine = ruleEngine;
        this.sseEmitterService = sseEmitterService;
    }

    @JmsListener(destination = "${sensor.queue}")
    public void onMessage(String message){
        System.out.println(message);
        try {
            SensorEvent sensorEvent = objectMapper.readValue(message, SensorEvent.class);
            sensorCache.update(sensorEvent);

            System.out.println("Message received from queue:");
            System.out.println(message);
            System.out.println("Cached sensor" + sensorEvent.getSensorId());

            ruleEngine.evaluate(sensorEvent);
            sseEmitterService.sendToAll(sensorEvent);



        } catch (Exception e){
            System.err.println("Failed to parse message: " + e.getMessage());
        }

    }
}

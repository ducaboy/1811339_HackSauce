package com.hacksause.processing.processingservice.consumer;

import com.hacksause.processing.processingservice.actuator.ActuatorClient;
import com.hacksause.processing.processingservice.cache.SensorCache;
import com.hacksause.processing.processingservice.engine.RuleEngine;
import com.hacksause.processing.processingservice.model.SensorEvent;
import com.hacksause.processing.processingservice.sse.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

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
    public void onMessage(jakarta.jms.Message message) {
        try {
            String json;

            if (message instanceof jakarta.jms.BytesMessage bytesMessage) {
                byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
                bytesMessage.readBytes(bytes);
                json = new String(bytes, StandardCharsets.UTF_8);
            } else if (message instanceof jakarta.jms.TextMessage textMessage) {
                json = textMessage.getText();
            } else {
                System.out.println("Unknown message type");
                return;
            }

            // System.out.println("Received message: " + json);

            SensorEvent sensorEvent = objectMapper.readValue(json, SensorEvent.class);
            sensorCache.update(sensorEvent);
            ruleEngine.evaluate(sensorEvent);
            sseEmitterService.sendToAll(sensorEvent);

        } catch (Exception e) {
            System.out.println("Failed to parse message:" + e.getMessage());
        }
    }
}

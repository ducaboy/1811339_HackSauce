package com.hacksause.processing.processingservice.engine;

import com.hacksause.processing.processingservice.actuator.ActuatorClient;
import com.hacksause.processing.processingservice.model.Rule;
import com.hacksause.processing.processingservice.model.SensorEvent;
import com.hacksause.processing.processingservice.repository.RuleRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RuleEngine {

    private final RuleRepository ruleRepository;
    private final ActuatorClient actuatorClient;

    public RuleEngine(RuleRepository ruleRepository, ActuatorClient actuatorClient){
        this.ruleRepository = ruleRepository;
        this.actuatorClient = actuatorClient;
    }

    public void evaluate(SensorEvent sensorEvent){

        List<Rule> rules = ruleRepository.findBySensorId(sensorEvent.getSensorId())
                .stream()
                .filter(Rule::isEnabled)
                .toList();

        for (Rule rule : rules) {
            sensorEvent.getMeasurements().stream()
                    .filter(m -> m.getMetric().equals(rule.getMetric()))
                    .findFirst()
                    .ifPresent(measurement -> {
                        if (matches(measurement.getValue(), rule.getOperator(), rule.getThreshold())) {
                            actuatorClient.trigger(rule.getActuatorName(), rule.getActuatorState());
                        }
                    });
        }
    }

    private boolean matches(double value, String operator, double threshold) {
        return switch (operator) {
            case ">"  -> value > threshold;
            case ">=" -> value >= threshold;
            case "<"  -> value < threshold;
            case "<=" -> value <= threshold;
            case "="  -> value == threshold;
            default   -> false;
        };
    }
}
package com.hacksause.processing.processingservice.controller;

import com.hacksause.processing.processingservice.actuator.ActuatorClient;
import com.hacksause.processing.processingservice.cache.SensorCache;
import com.hacksause.processing.processingservice.model.Rule;
import com.hacksause.processing.processingservice.model.SensorEvent;
import com.hacksause.processing.processingservice.repository.RuleRepository;
import com.hacksause.processing.processingservice.sse.SseEmitterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    private final SensorCache sensorCache;
    private final RuleRepository ruleRepository;
    private final ActuatorClient actuatorClient;
    private final SseEmitterService sseEmitterService;

    public ApiController(SensorCache sensorCache, RuleRepository ruleRepository,
                         ActuatorClient actuatorClient, SseEmitterService sseEmitterService) {
        this.sensorCache = sensorCache;
        this.ruleRepository = ruleRepository;
        this.actuatorClient = actuatorClient;
        this.sseEmitterService = sseEmitterService;
    }

    // GET all sensor states from cache
    @GetMapping("/sensors")
    public ResponseEntity<Map<String, SensorEvent>> getSensors() {
        return ResponseEntity.ok(sensorCache.getAll());
    }

    // GET all actuator states
    @GetMapping("/actuators")
    public ResponseEntity<?> getActuators() {
        return ResponseEntity.ok(actuatorClient.getAllStates());
    }

    // POST manual actuator toggle
    @PostMapping("/actuators/{name}/toggle")
    public ResponseEntity<?> toggleActuator(@PathVariable String name, @RequestBody Map<String, String> body) {
        String state = body.get("state");
        actuatorClient.trigger(name, state);
        return ResponseEntity.ok().build();
    }

    // GET rules for specific actuator
    @GetMapping("/actuators/{name}/rules")
    public ResponseEntity<?> getRulesByActuator(@PathVariable String name) {
        return ResponseEntity.ok(ruleRepository.findByActuatorName(name));
    }

    // POST create rule for actuator
    @PostMapping("/actuators/{name}/rules")
    public ResponseEntity<?> createRule(@PathVariable String name, @RequestBody Rule rule) {
        rule.setActuatorName(name);
        return ResponseEntity.ok(ruleRepository.save(rule));
    }

    // DELETE a rule
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<?> deleteRule(@PathVariable Long id) {
        ruleRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // PUT toggle rule enabled/disabled
    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<?> toggleRule(@PathVariable Long id) {
        return ruleRepository.findById(id).map(rule -> {
            rule.setEnabled(!rule.isEnabled());
            ruleRepository.save(rule);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/events")
    public SseEmitter streamEvents() {
        return sseEmitterService.addEmitter();
    }
}
package com.hacksause.processing.processingservice;

import com.hacksause.processing.processingservice.model.Rule;
import com.hacksause.processing.processingservice.repository.RuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final RuleRepository ruleRepository;

    public DataInitializer(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ruleRepository.count() > 0) return; // don't re-seed if rules already exist

        ruleRepository.save(new Rule("greenhouse_temperature", "temperature_c", ">", 30.0, "cooling_fan", "ON"));
        ruleRepository.save(new Rule("greenhouse_temperature", "temperature_c", "<", 18.0, "habitat_heater", "ON"));
        ruleRepository.save(new Rule("entrance_humidity", "humidity_pct", "<", 25.0, "entrance_humidifier", "ON"));
        ruleRepository.save(new Rule("entrance_humidity", "humidity_pct", ">", 70.0, "entrance_humidifier", "OFF"));
        ruleRepository.save(new Rule("co2_hall", "co2_ppm", ">", 1200.0, "hall_ventilation", "ON"));
    }
}
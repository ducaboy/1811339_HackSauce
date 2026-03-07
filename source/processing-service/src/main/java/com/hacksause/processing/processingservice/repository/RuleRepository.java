package com.hacksause.processing.processingservice.repository;

import com.hacksause.processing.processingservice.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findBySensorId(String sensorId);
    List<Rule> findByActuatorName(String actuatorName);
}

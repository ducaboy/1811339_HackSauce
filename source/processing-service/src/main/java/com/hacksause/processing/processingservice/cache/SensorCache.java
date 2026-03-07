package com.hacksause.processing.processingservice.cache;

import com.hacksause.processing.processingservice.model.SensorEvent;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SensorCache {

    private final Map<String, SensorEvent> cache = new ConcurrentHashMap<>();

    public void update(SensorEvent event){
        cache.put(event.getSensorId(),event);
    }

    public Map<String, SensorEvent> getAll(){
        return Collections.unmodifiableMap(cache);
    }
}

package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@AsyncHealth(refreshRate = 2)
@Component
public class UpIndicator2 implements HealthIndicator {

    @Override
    public Health health() {
        return Health
            .up()
            .build();
    }

}

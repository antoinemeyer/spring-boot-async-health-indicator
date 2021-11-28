package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("with-expensive-indicator")
@Component
@AsyncHealth(1)
public class ExpensiveIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }
        return Health
            .up()
            .withDetail("detailKey", "detailValue")
            .build();
    }
    
}

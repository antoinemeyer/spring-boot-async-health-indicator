package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("with-timing-out-indicator")
@Component
@AsyncHealth(refreshRate = 1, timeout = 1)
public class TimingOutSleepingIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Health
            .up()
            .withDetail("detailKey", "detailValue")
            .build();
    }

}

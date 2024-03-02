package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("with-timing-out-indicator-interruption-disabled")
@Component
@AsyncHealth(refreshRate = 1, timeout = 1, interruptOnTimeout = false)
public class TimingOutSleepingIndicatorInterruptionDisabled implements HealthIndicator {

    public static final int SLEEP_DURATION = 2000;

    @Override
    public Health health() {
        try {
            Thread.sleep(SLEEP_DURATION);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return Health
            .up()
            .withDetail("detailKey", "detailValue")
            .build();
    }

}

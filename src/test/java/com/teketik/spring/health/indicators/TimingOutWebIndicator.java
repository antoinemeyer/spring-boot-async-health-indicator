package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("with-timing-out-web-indicator")
@Component
@AsyncHealth(refreshRate = 1, timeout = 1)
public class TimingOutWebIndicator implements HealthIndicator {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Override
    public Health health() {
        testRestTemplate.getForEntity("/", Void.class);
        return Health.up().build();
    }

}

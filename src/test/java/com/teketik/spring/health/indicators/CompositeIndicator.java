package com.teketik.spring.health.indicators;

import com.teketik.spring.health.AsyncHealth;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Iterator;

@AsyncHealth
@Component
public class CompositeIndicator implements CompositeHealthContributor {

    private final HealthIndicator healthIndicator = new HealthIndicator() {
        @Override
        public Health health() {
            return Health.up().build();
        }
    };

    @Override
    public HealthContributor getContributor(String name) {
        throw new AssertionError();
    }

    @Override
    public Iterator<NamedContributor<HealthContributor>> iterator() {
        return Collections
            .<NamedContributor<HealthContributor>>singleton(NamedContributor.of("name", healthIndicator))
            .iterator();
    }

}

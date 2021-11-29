package com.teketik.spring.health;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.time.LocalDateTime;
import java.util.Optional;

class AsyncHealthIndicator implements HealthIndicator, Runnable {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String LAST_CHECK_KEY = "lastChecked";
    private static final String LAST_DURATION_KEY = "lastDuration";

    private static final Health UNKOWN_HEALTH = Health.unknown().build();

    private final HealthIndicator originalHealthIndicator;
    private final String name;

    private volatile Health lastHealth;

    public AsyncHealthIndicator(HealthIndicator originalHealthIndicator, String name) {
        this.originalHealthIndicator = originalHealthIndicator;
        this.name = name;
    }

    @Override
    public void run() {
        if (logger.isTraceEnabled()) {
            logger.trace("Refreshing " + name);
        }
        long currentTimeMillis = System.currentTimeMillis();
        try {
            final Health originalHealth = this.originalHealthIndicator.health();
            final String executionTime = makeFormattedExecutionTime(currentTimeMillis);
            if (logger.isTraceEnabled()) {
                logger.trace(name + " computed in " + executionTime + " is " + originalHealth);
            }
            this.lastHealth = Health
                .status(originalHealth.getStatus())
                .withDetails(originalHealth.getDetails())
                .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                .withDetail(LAST_DURATION_KEY, executionTime)
                .build();
        } catch (Exception e) {
            logger.error("Error while refreshing healthIndicator " + name, e);
            this.lastHealth = Health
                .status(Status.DOWN)
                .withException(e)
                .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                .withDetail(LAST_DURATION_KEY, makeFormattedExecutionTime(currentTimeMillis))
                .build();
        }
    }

    private String makeFormattedExecutionTime(long currentTimeMillis) {
        return (System.currentTimeMillis() - currentTimeMillis) + "ms";
    }

    @Override
    public Health health() {
        return Optional
            .ofNullable(lastHealth)
            .orElse(UNKOWN_HEALTH);
    }

}

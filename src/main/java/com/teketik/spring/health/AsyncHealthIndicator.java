package com.teketik.spring.health;

import com.teketik.utils.Schedulable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class AsyncHealthIndicator implements HealthIndicator, Schedulable {

    private final Log logger = LogFactory.getLog(getClass());

    private static final String LAST_CHECK_KEY = "lastChecked";
    private static final String LAST_DURATION_KEY = "lastDuration";
    private static final String REASON_KEY = "reason";

    private static final Health UNKNOWN_HEALTH = Health.unknown().build();

    private final HealthIndicator originalHealthIndicator;
    private final String name;
    private final int refreshRateInSeconds;
    private final int timeoutInSeconds;

    private volatile Health lastHealth;
    private volatile long healthStartTimeMillis = -1;

    public AsyncHealthIndicator(HealthIndicator originalHealthIndicator, String name, int refreshRateInSeconds, int timeoutInSeconds) {
        this.originalHealthIndicator = originalHealthIndicator;
        this.name = name;
        this.refreshRateInSeconds = refreshRateInSeconds;
        this.timeoutInSeconds = timeoutInSeconds;
    }

    @Override
    public void run() {
        if (logger.isTraceEnabled()) {
            logger.trace("Refreshing " + name);
        }
        this.healthStartTimeMillis = System.currentTimeMillis();
        try {
            final Health originalHealth = this.originalHealthIndicator.health();
            final long executionTime = System.currentTimeMillis() - this.healthStartTimeMillis;
            this.lastHealth = checkForTimeout(executionTime)
                .orElseGet(() -> {
                    final String formattedExecutionTime = executionTime + "ms";
                    if (logger.isDebugEnabled()) {
                        logger.debug("HealthIndicator[name=" + name + "][duration=" + formattedExecutionTime + "][status=" + originalHealth + "]");
                    }
                    return Health
                        .status(originalHealth.getStatus())
                        .withDetails(originalHealth.getDetails())
                        .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                        .withDetail(LAST_DURATION_KEY, formattedExecutionTime)
                        .build();
                });
        } catch (Exception e) {
            final String formattedExecutionTime = (System.currentTimeMillis() - this.healthStartTimeMillis) + "ms";
            logger.error("Error while refreshing HealthIndicator[name=" + name + "][duration=" + formattedExecutionTime + "]", e);
            this.lastHealth = Health
                .status(Status.DOWN)
                .withException(e)
                .withDetail(REASON_KEY, "Exception")
                .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                .withDetail(LAST_DURATION_KEY, formattedExecutionTime)
                .build();
        }
        this.healthStartTimeMillis = -1;
    }

    @Override
    public Health health() {
        final long startTimeMillis = this.healthStartTimeMillis;
        if (startTimeMillis != -1) {
            final Optional<Health> timeout = checkForTimeout(System.currentTimeMillis() - startTimeMillis);
            if (timeout.isPresent()) {
                return timeout.get();
            }
        }
        if (lastHealth != null) {
            return lastHealth;
        }
        return UNKNOWN_HEALTH;
    }

    private Optional<Health> checkForTimeout(final long currentDuration) {
        if (currentDuration > TimeUnit.SECONDS.toMillis(timeoutInSeconds)) {
            logger.error("HealthIndicator[name=" + name + "] is taking too long to execute [duration="
                + currentDuration + "ms][timeout=" + timeoutInSeconds + "s]");
            return Optional.of(
                Health
                    .status(Status.DOWN)
                    .withDetail(REASON_KEY, "Timeout")
                    .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                    .withDetail(LAST_DURATION_KEY, currentDuration + "ms")
                    .build()
            );
        }
        return Optional.empty();
    }

    @Override
    public int getRefreshRateInSeconds() {
        return refreshRateInSeconds;
    }

    @Override
    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    @Override
    public String toString() {
        return "AsyncHealthIndicator[name=" + name + "][refreshRate=" + refreshRateInSeconds + "s][timeout=" + timeoutInSeconds + "s]";
    }

}

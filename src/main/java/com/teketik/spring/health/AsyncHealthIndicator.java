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

    private static final Health UNKOWN_HEALTH = Health.unknown().build();

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
            final String executionTime = makeFormattedExecutionTime(this.healthStartTimeMillis);
            if (logger.isDebugEnabled()) {
                logger.debug(name + " computed in " + executionTime + " is " + originalHealth);
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
                .withDetail(REASON_KEY, "Exception")
                .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                .withDetail(LAST_DURATION_KEY, makeFormattedExecutionTime(this.healthStartTimeMillis))
                .build();
        }
        this.healthStartTimeMillis = -1;
    }

    private String makeFormattedExecutionTime(long startTimeMillis) {
        return (System.currentTimeMillis() - startTimeMillis) + "ms";
    }

    @Override
    public Health health() {
        final long startTimeMillis = this.healthStartTimeMillis;
        if (startTimeMillis != -1) {
            final long currentDuration = System.currentTimeMillis() - startTimeMillis;
            if (currentDuration > TimeUnit.SECONDS.toMillis(timeoutInSeconds)) {
                logger.error("HealthIndicator " + name + " took too long to execute [duration="
                    + currentDuration + "ms][timeout=" + timeoutInSeconds + "s]");
            return Health
                    .status(Status.DOWN)
                    .withDetail(REASON_KEY, "Timeout")
                    .withDetail(LAST_CHECK_KEY, LocalDateTime.now())
                    .withDetail(LAST_DURATION_KEY, currentDuration + "ms")
                    .build();
            }
        }
        return Optional
            .ofNullable(lastHealth)
            .orElse(UNKOWN_HEALTH);
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
        return "AsyncHealthIndicator [name=" + name + ", refreshRate=" + refreshRateInSeconds + "s, timeout=" + timeoutInSeconds + "s]";
    }

}

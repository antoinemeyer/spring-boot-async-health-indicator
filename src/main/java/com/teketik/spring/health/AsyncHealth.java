package com.teketik.spring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>Makes a {@link HealthIndicator} {@link HealthIndicator#health() health method} periodically executed on a background {@link ThreadPoolExecutor}.<br>
 * <p>The {@link HealthIndicator#health()} health method:
 * <ul>
 * <li>is executed with the given delay configured by {@link AsyncHealth#refreshRate() refreshRate()} (in second) between the termination of one execution and the commencement of the next.</li>
 * <li>may not execute in more than the time configured by {@link AsyncHealth#timeout() timeout()} (in second). Passed this delay, the thread running the {@link HealthIndicator} will be {@link Thread#interrupt() interrupted}
 * and, if interruptible, the {@link Health} will be set as {@link Status#DOWN} until the next execution.</li>
 * </ul>
 * <p>The `/health` endpoint will always return the last {@link Health}.
 * <p>Note that the {@link HealthIndicator} may return {@link Status#UNKNOWN} on application startup if the `/health` endpoint
 * is called before it has completed its first {@link HealthIndicator#health()} check.
 * <br>
 * <p>Example:<br>
 * In this example, myIndicator.health() will be executed every two seconds and time out if the method takes more than 5 seconds to execute:
 * <pre class="code">
 * &#064;AsyncHealth(refreshRate = 2, timeout = 5)
 * &#064;Component
 * public class MyIndicator implements HealthIndicator {
 *     &#064;Override
 *     public Health health() {
 *         return Health.up().build();
 *     }
 * }
 * </pre>
 * <p>
 * See <a href="https://github.com/antoinemeyer/spring-boot-async-health-indicator">https://github.com/antoinemeyer/spring-boot-async-health-indicator</a>
 * @author Antoine Meyer
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AsyncHealth {

    /**
     * @return the time to wait in seconds between {@link HealthIndicator#health()} executions. (between the termination of
     * one execution and the commencement of the next)
     */
    int refreshRate() default 1;

    /**
     * @return the maximum time in seconds that {@link HealthIndicator#health()} can run for.
     */
    int timeout() default 10;

}


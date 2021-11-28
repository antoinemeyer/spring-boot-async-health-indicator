package com.teketik.spring.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * <p>Makes a {@link HealthIndicator} {@link HealthIndicator#health() health method} asynchronously refreshed on a background {@link ScheduledThreadPoolExecutor}.<br>
 * <p>The {@link HealthIndicator#health() health method} is invoked with the given delay (in second) between the termination of one execution and the commencement of the next. 
 * <p>The `/health` endpoint will always return the last {@link Health}.
 * <p>Note that the {@link HealthIndicator} may return {@link Status#UNKNOWN} on application startup if the `/health` endpoint
 * is called before it has completed its first {@link HealthIndicator#health()} check.
 * <br>
 * <p>Example:<br>
 * In this example, myIndicator.health() will be called every two seconds  
 * <pre class="code">
 * &#064;AsyncHealth(2)
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
     * @return the time to wait in seconds between {@link HealthIndicator#health()} invocations. (between the termination of 
     * one execution and the commencement of the next)
     */
    int value() default 1;

}


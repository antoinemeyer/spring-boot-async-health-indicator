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
 * <p>The {@link HealthIndicator#health()} method:
 * <ul>
 * <li>is executed with the given delay configured by {@link AsyncHealth#refreshRate() refreshRate()} between the termination of one execution and the commencement of the next.</li>
 * <li>may not execute in more than the time configured by {@link AsyncHealth#timeout() timeout()}. Passed this delay, the {@link Health} will be set as {@link Status#DOWN} until the next execution
 * and the thread running the {@link HealthIndicator} will be {@link Thread#interrupt() interrupted} if {@link #interruptOnTimeout()} is {@code true}.</li>
 * </ul>
 * <p>The `/health` endpoint will not invoke the {@link HealthIndicator#health() health method} but return the last {@link Health} calculated asynchronously.
 * <p><i>Note that the {@link HealthIndicator} may return {@link Status#UNKNOWN} on application startup if the `/health` endpoint
 * is called before it has completed its first {@link HealthIndicator#health()} check.</i>
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
 * <hr>
 * <strong>Regarding Timeout</strong>
 * <p>
 * When a {@link HealthIndicator#health() health method} duration exceeds the configured {@link #timeout()}, the thread running it is <strong>interrupted</strong> if {@link #interruptOnTimeout()} is {@code true}
 * with the hope that the method will fail with an exception (causing it to be {@link Status#DOWN}) and free up the thread.<br>
 * Unfortunately, most I/O calls are not interruptible and the thread may continue to execute the method until it times out (according to the libraries and configuration used).
 * <p>
 * If that happens, you will observe the 'timeout' error message printed for each `/health` hit until that method times out like:
 * <pre class="code">
 * ERROR AsyncHealthIndicator   : HealthIndicator[name=myIndicator] is taking too long to execute [duration=2121ms][timeout=2s]
 * ERROR AsyncHealthIndicator   : HealthIndicator[name=myIndicator] is taking too long to execute [duration=3189ms][timeout=2s]
 * </pre>
 * It is therefore recommended to ensure that your {@link HealthIndicator#health() health method}s can time out naturally within an acceptable window (matching the configured {@link #timeout()})
 * <hr>
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
     * @return the maximum time in seconds that {@link HealthIndicator#health()} can run for before being considered {@link Status#DOWN}.
     */
    int timeout() default 10;

    /**
     * @return whether the thread should be interrupted when a timeout occurs. (if {@code false}, the next check will only be scheduled after the current execution terminates naturally).
     */
    boolean interruptOnTimeout() default true;

}


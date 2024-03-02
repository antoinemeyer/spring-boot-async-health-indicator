package com.teketik.spring.health;

import com.teketik.utils.SchedulingThreadPoolExecutor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

@TestPropertySource(properties = {
    "management.health.async.pool.max-size=7",
    "management.health.async.pool.keep-alive=8",
})
public class ConfigurationTest extends BaseITest {

    @Autowired
    private AsyncHealthIndicatorAutoConfiguration asyncHealthIndicatorAutoConfiguration;

    @Test
    public void test() {
        final SchedulingThreadPoolExecutor schedulingThreadPoolExecutor = (SchedulingThreadPoolExecutor) ReflectionTestUtils
            .getField(asyncHealthIndicatorAutoConfiguration, "schedulingThreadPoolExecutor");
        Assertions.assertEquals(7, schedulingThreadPoolExecutor.getMaximumPoolSize());
        Assertions.assertEquals(8, schedulingThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
    }

}

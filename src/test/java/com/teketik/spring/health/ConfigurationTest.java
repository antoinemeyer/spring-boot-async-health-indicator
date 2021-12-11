package com.teketik.spring.health;

import com.teketik.utils.SchedulingThreadPoolExecutor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

@TestPropertySource(properties = {
    "management.health.async.pool.max-size=7",
    "management.health.async.pool.keep-alive=8",
})
public class ConfigurationTest extends BaseITest {

    @Resource
    private AsyncHealthIndicatorAutoConfiguration asyncHealthIndicatorAutoConfiguration;

    @Test
    public void test() {
        final SchedulingThreadPoolExecutor schedulingThreadPoolExecutor = (SchedulingThreadPoolExecutor) ReflectionTestUtils
            .getField(asyncHealthIndicatorAutoConfiguration, "schedulingThreadPoolExecutor");
        Assert.assertEquals(7, schedulingThreadPoolExecutor.getMaximumPoolSize());
        Assert.assertEquals(8, schedulingThreadPoolExecutor.getKeepAliveTime(TimeUnit.SECONDS));
    }

}

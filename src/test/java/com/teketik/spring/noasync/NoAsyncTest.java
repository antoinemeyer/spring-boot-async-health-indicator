package com.teketik.spring.noasync;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class NoAsyncTest {

    @Autowired
    protected HealthContributorRegistry healthContributorRegistry;

    @Test
    public void testInstanceOf() {
        Assert.assertTrue(healthContributorRegistry.getContributor("defaultIndicator") instanceof HealthIndicator);
    }
}

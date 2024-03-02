package com.teketik.spring.noasync;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class NoAsyncTest {

    @Autowired
    protected HealthContributorRegistry healthContributorRegistry;

    @Test
    public void testInstanceOf() {
        Assertions.assertTrue(healthContributorRegistry.getContributor("defaultIndicator") instanceof HealthIndicator);
    }
}

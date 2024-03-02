package com.teketik.spring.health;


import org.assertj.core.matcher.AssertionMatcher;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.time.LocalDateTime;

public class LastCheckedITest extends BaseITest {

    @Test
    public void testInstanceOf() {
        Assertions.assertTrue(healthContributorRegistry.getContributor("defaultIndicator") instanceof HealthIndicator);
        Assertions.assertTrue(healthContributorRegistry.getContributor("upIndicator1") instanceof AsyncHealthIndicator);
        Assertions.assertTrue(healthContributorRegistry.getContributor("upIndicator2") instanceof AsyncHealthIndicator);
    }

    @Test
    public void testUnknown() throws Exception {
        ReflectionTestUtils.setField(healthContributorRegistry.getContributor("upIndicator1"), "lastHealth", null);
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UNKNOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details").doesNotExist());
    }

    @Test
    public void testUpIndicator1Statuses() throws Exception {
        // wait until health computed
        Awaitility.await().until(() -> {
            return ReflectionTestUtils.getField(healthContributorRegistry.getContributor("upIndicator1"), "lastHealth") != null;
        });
        //first hit is up
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime[] firstLastChecked = new LocalDateTime[1];
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.detailKey").value("detailValue"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        firstLastChecked[0] = LocalDateTime.parse(actual);
                        final int difference = (int) Duration.between(firstLastChecked[0], now).toMillis();
                        MatcherAssert.assertThat(difference, CoreMatchers.allOf(
                            Matchers.greaterThanOrEqualTo(0),
                            Matchers.lessThan(1000)
                        ));
                    }
                }
            ));
        //second hit is cached
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.detailKey").value("detailValue"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final LocalDateTime secondHit = LocalDateTime.parse(actual);
                        Assertions.assertEquals(firstLastChecked[0], secondHit);
                    }
                }
            ));
        Thread.sleep(1000);
        //wait until new status on third hit
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.detailKey").value("detailValue"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final LocalDateTime thirdHit = LocalDateTime.parse(actual);
                        final int difference = (int) Duration.between(firstLastChecked[0], thirdHit).toMillis();
                        MatcherAssert.assertThat(difference, Matchers.greaterThan(0));
                    }
                }
            ));
    }

}

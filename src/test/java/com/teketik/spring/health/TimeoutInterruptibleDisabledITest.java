package com.teketik.spring.health;

import org.assertj.core.matcher.AssertionMatcher;
import org.awaitility.Awaitility;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.time.LocalDateTime;

@ActiveProfiles("with-timing-out-indicator-interruption-disabled")
public class TimeoutInterruptibleDisabledITest extends BaseITest {

    @Test
    public void test() throws Exception {
        final LocalDateTime[] upIndicator1FirstLastChecked = new LocalDateTime[1];
        final LocalDateTime[] timingOutSleepingIndicatorInterruptionDisabledFirstLastChecked = new LocalDateTime[1];
        Awaitility.await().untilAsserted(() -> {
                mockMvc
                    .perform(MockMvcRequestBuilders.get("/actuator/health"))
                    .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.status").value(CoreMatchers.not("UNKNOWN")));
        });
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.status").value("DOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.error").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.reason").value(Matchers.equalTo("Timeout")))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastDuration").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        Assertions.assertEquals(6, actual.length());
                        Assertions.assertTrue(actual.endsWith("ms"));
                        MatcherAssert.assertThat(actual, CoreMatchers.startsWith("1"));
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        upIndicator1FirstLastChecked[0] = LocalDateTime.parse(actual);
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        timingOutSleepingIndicatorInterruptionDisabledFirstLastChecked[0] = LocalDateTime.parse(actual);
                    }
                }
            ));
        Thread.sleep(1000);
        //still the same
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.status").value("DOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.error").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.reason").value(Matchers.equalTo("Timeout")))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastDuration").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        Assertions.assertEquals(6, actual.length());
                        Assertions.assertTrue(actual.endsWith("ms"));
                        MatcherAssert.assertThat(actual, CoreMatchers.startsWith("2"));
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final int difference = (int) Duration.between(upIndicator1FirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                        MatcherAssert.assertThat(difference, Matchers.greaterThan(0));
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final int difference = (int) Duration.between(timingOutSleepingIndicatorInterruptionDisabledFirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                        MatcherAssert.assertThat(difference, Matchers.equalTo(0));
                    }
                }
            ));
        Thread.sleep(2000);
        //then update last check
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.status").value("DOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.error").doesNotExist())
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.reason").value(Matchers.equalTo("Timeout")))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastDuration").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        Assertions.assertEquals(6, actual.length());
                        Assertions.assertTrue(actual.endsWith("ms"));
                        MatcherAssert.assertThat(actual, CoreMatchers.startsWith("1"));
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final int difference = (int) Duration.between(upIndicator1FirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                        MatcherAssert.assertThat(difference, Matchers.greaterThan(0));
                    }
                }
            ))
            .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutSleepingIndicatorInterruptionDisabled.details.lastChecked").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        final int difference = (int) Duration.between(timingOutSleepingIndicatorInterruptionDisabledFirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                        MatcherAssert.assertThat(difference, Matchers.greaterThan(0));
                    }
                }
            ));

    }

}

package com.teketik.spring.health;

import org.assertj.core.matcher.AssertionMatcher;
import org.awaitility.Awaitility;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Duration;
import java.time.LocalDateTime;

@ActiveProfiles("with-timing-out-web-indicator")
public class TimeoutWebITest extends BaseITest {

    @Test
    public void test() throws InterruptedException {
        final LocalDateTime[] upIndicator1FirstLastChecked = new LocalDateTime[1];
        final LocalDateTime[] timingOutIndicatorFirstLastChecked = new LocalDateTime[1];
        Awaitility.await().untilAsserted(() -> {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.status").value("DOWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.error").value(Matchers.containsString("InterruptedException")))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.lastDuration").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            Assert.assertEquals(6, actual.length());
                            Assert.assertTrue(actual.endsWith("ms"));
                            Assert.assertTrue(actual.startsWith("1"));
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
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.lastChecked").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            timingOutIndicatorFirstLastChecked[0] = LocalDateTime.parse(actual);
                        }
                    }
                ));
        });
        Awaitility.await().untilAsserted(() -> {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.status").value("DOWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.error").value(Matchers.containsString("InterruptedException")))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.lastDuration").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            Assert.assertEquals(6, actual.length());
                            Assert.assertTrue(actual.endsWith("ms"));
                            Assert.assertTrue(actual.startsWith("1"));
                        }
                    }
                ))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastChecked").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            final int difference = (int) Duration.between(upIndicator1FirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                            Assert.assertThat(difference, Matchers.greaterThan(0));

                        }
                    }
                ))
                .andExpect(MockMvcResultMatchers.jsonPath("components.timingOutWebIndicator.details.lastChecked").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            final int difference = (int) Duration.between(timingOutIndicatorFirstLastChecked[0], LocalDateTime.parse(actual)).toMillis();
                            Assert.assertThat(difference, Matchers.greaterThan(0));
                        }
                    }
                ));
        });
    }

}

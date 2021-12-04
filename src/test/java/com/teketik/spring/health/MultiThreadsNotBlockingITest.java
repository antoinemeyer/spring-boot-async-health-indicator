package com.teketik.spring.health;

import org.awaitility.Awaitility;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.concurrent.TimeUnit;

@ActiveProfiles("with-expensive-indicator")
public class MultiThreadsNotBlockingITest extends BaseITest {

    @Test
    public void testExpensiveIndicatorNotBlockingOtherIndicators() throws Exception {
        ReflectionTestUtils.setField(healthContributorRegistry.getContributor("upIndicator2"), "lastHealth", null);
        ReflectionTestUtils.setField(healthContributorRegistry.getContributor("expensiveIndicator"), "lastHealth", null);
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator2.status").value("UP"));
        });
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.expensiveIndicator.status").value("UNKNOWN"));
    }
}

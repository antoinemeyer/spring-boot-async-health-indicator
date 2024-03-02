package com.teketik.spring.health;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@ActiveProfiles("with-expensive-indicator")
@TestPropertySource(properties = "management.health.async.pool.max-size=1")
public class MultiThreadsBlockingITest extends BaseITest {

    @Test
    public void testExpensiveIndicatorBlockingOtherIndicators() throws Exception {
        Thread.sleep(3000);
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.expensiveIndicator.status").value("UNKNOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UNKNOWN"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator2.status").value("UNKNOWN"));
    }
}

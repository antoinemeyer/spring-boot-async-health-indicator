package com.teketik.spring.health;

import org.junit.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@TestPropertySource(properties = "logging.level.com.teketik=INFO")
public class DefaultIndicatorITest extends BaseITest {

    @Test
    public void test() throws Exception {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.defaultIndicator.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.defaultIndicator.details").doesNotExist());
    }

}

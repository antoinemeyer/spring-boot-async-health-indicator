package com.teketik.spring.health;

import com.teketik.spring.health.indicators.UpIndicator1;

import org.assertj.core.matcher.AssertionMatcher;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class DownIndicatorITest extends BaseITest {

    @SpyBean
    private UpIndicator1 upIndicator1;
    
    @Test
    public void testDown() throws Exception {
        Mockito
            .doThrow(new RuntimeException())
            .doCallRealMethod()
            .when(upIndicator1).health();
        Awaitility.await().untilAsserted(() -> {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("DOWN"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.detailKey").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastDuration").value(
                    new AssertionMatcher<String>() {
                        @Override
                        public void assertion(String actual) throws AssertionError {
                            final String[] split = actual.split("s");
                            Assert.assertEquals("0", split[0]);
                        }
                    }
                ));
        });
        // assert an exception can be recovered in subsequent states
        Awaitility.await().untilAsserted(() -> {
            mockMvc
                .perform(MockMvcRequestBuilders.get("/actuator/health"))
                .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"));
        });
    }
    
}

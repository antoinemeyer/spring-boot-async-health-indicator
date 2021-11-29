package com.teketik.spring.health;

import com.teketik.spring.health.indicators.UpIndicator1;

import org.assertj.core.matcher.AssertionMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LastDurationITest extends BaseITest {

    @SpyBean
    private UpIndicator1 upIndicator1;

    @Test
    public void testLastDuration() throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        Mockito.doAnswer(a -> {
            Thread.sleep(1000);
            cdl.countDown();
            return a.callRealMethod();
        }).when(upIndicator1).health();
        cdl.await(3, TimeUnit.SECONDS);
        mockMvc
            .perform(MockMvcRequestBuilders.get("/actuator/health"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.status").value("UP"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.detailKey").value("detailValue"))
            .andExpect(MockMvcResultMatchers.jsonPath("components.upIndicator1.details.lastDuration").value(
                new AssertionMatcher<String>() {
                    @Override
                    public void assertion(String actual) throws AssertionError {
                        Assert.assertEquals(6, actual.length());
                        Assert.assertTrue(actual.endsWith("ms"));
                        Assert.assertTrue(actual.startsWith("1"));
                    }
                }
            ));

    }
}

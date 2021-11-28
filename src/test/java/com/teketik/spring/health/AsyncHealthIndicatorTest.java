package com.teketik.spring.health;


import org.junit.Assert;
import org.junit.Test;

public class AsyncHealthIndicatorTest {

    @Test
    public void testMakeFormattedDifference() {
        Assert.assertEquals("0s000", AsyncHealthIndicator.makeFormattedDifference(0, 0));
        Assert.assertEquals("0s001", AsyncHealthIndicator.makeFormattedDifference(1, 0));
        Assert.assertEquals("0s667", AsyncHealthIndicator.makeFormattedDifference(1234, 567));
        Assert.assertEquals("123333s333", AsyncHealthIndicator.makeFormattedDifference(123456789, 123456));
    }
    
}

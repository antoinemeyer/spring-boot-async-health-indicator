package com.teketik.utils;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.ScheduledExecutorService;

public class SchedulingThreadPoolExecutorTest {

    @Test
    public void testShutdownNow() {
        final SchedulingThreadPoolExecutor schedulingThreadPoolExecutor = new SchedulingThreadPoolExecutor(1, 1);
        final ScheduledExecutorService mock = Mockito.mock(ScheduledExecutorService.class);
        ReflectionTestUtils.setField(schedulingThreadPoolExecutor, "coordinatorExecutorService", mock);
        schedulingThreadPoolExecutor.shutdownNow();
        Mockito.verify(mock).shutdownNow();
    }

}

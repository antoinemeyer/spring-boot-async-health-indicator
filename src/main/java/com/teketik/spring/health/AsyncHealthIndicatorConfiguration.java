package com.teketik.spring.health;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.health.async")
class AsyncHealthIndicatorConfiguration {

    private int poolSize = 1;
    
    public AsyncHealthIndicatorConfiguration() {
    }

    public int getPoolSize() {
        return poolSize;
    }
    
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}

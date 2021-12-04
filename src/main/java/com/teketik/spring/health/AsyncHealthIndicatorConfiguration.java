package com.teketik.spring.health;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.health.async.pool")
class AsyncHealthIndicatorConfiguration {

    private int maxSize = 10;

    private int keepAlive = 10;

    public AsyncHealthIndicatorConfiguration() {
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

}

package com.teketik.utils;

public interface Schedulable extends Runnable {
    int getRefreshRateInSeconds();
    int getTimeoutInSeconds();
}

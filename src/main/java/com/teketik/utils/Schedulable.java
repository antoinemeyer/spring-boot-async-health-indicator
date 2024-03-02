package com.teketik.utils;

import java.util.Optional;

public interface Schedulable extends Runnable {

    /**
     * @return time in seconds between the termination of one execution and the commencement of the next.
     */
    int getRefreshRateInSeconds();

    /**
     * @return maximum time in seconds this can run before being interrupted ({@link Optional#empty() empty} if never to be interrupted).
     */
    Optional<Integer> getTimeoutInSeconds();

}

package com.teketik.utils;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// timeout inspired by https://stackoverflow.com/q/2758612/1069426
public class SchedulingThreadPoolExecutor extends ThreadPoolExecutor {

    /*
     * Note that this is a classic ThreadPoolExecutor (not a ScheduledThreadPoolExecutor) to benefit from
     * dynamic pool sizing.
     * It is coordinated by a ScheduledThreadPoolExecutor handling cancellation and scheduling.
     */

    class TaskDecorator<V> extends FutureTask<V> {

        private final Schedulable runnable;

        public TaskDecorator(Schedulable runnable, V result) {
            super(runnable, result);
            this.runnable = runnable;
        }

    }

    private final ScheduledExecutorService coordinatorExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentMap<Runnable, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<Runnable, ScheduledFuture<?>>();

    public SchedulingThreadPoolExecutor(int maxCorePoolSize, long keepAliveTimeInSeconds) {
        super(1, maxCorePoolSize, keepAliveTimeInSeconds, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new CustomizableThreadFactory("async-health-"));
    }

    @Override
    public void shutdown() {
        coordinatorExecutorService.shutdown();
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        coordinatorExecutorService.shutdownNow();
        return super.shutdownNow();
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
        return new TaskDecorator<>((Schedulable) runnable, value);
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
        TaskDecorator decorator = (TaskDecorator) runnable;
        final ScheduledFuture<?> interrupterScheduledFuture = coordinatorExecutorService.schedule(
            () -> thread.interrupt(),
            decorator.runnable.getTimeoutInSeconds(),
            TimeUnit.SECONDS
        );
        runningTasks.put(runnable, interrupterScheduledFuture);
    }

    @Override
    protected void afterExecute(Runnable runnable, Throwable thread) {
        final TaskDecorator decorator = (TaskDecorator) runnable;
        Optional
            .ofNullable(runningTasks.remove(runnable))
            .ifPresent(runningTask -> runningTask.cancel(false));
        coordinatorExecutorService.schedule(
            () -> submit(decorator.runnable),
            decorator.runnable.getRefreshRateInSeconds(),
            TimeUnit.SECONDS
        );
    }

    public void run(Schedulable runnable) {
        coordinatorExecutorService.submit(() -> submit(runnable));
    }

}

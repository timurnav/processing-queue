package com.developer.timurnav.executor;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Callable;

public class SequentialClockTaskExecutor extends Thread implements ClockTaskExecutor {

    private final ClockTaskBlockingQueue queue;

    public SequentialClockTaskExecutor(ClockTaskBlockingQueue queue) {
        this.queue = queue;
    }

    @Override
    public <T> Promise<T> submit(LocalDateTime executionTime, Callable<T> task) {
        Objects.requireNonNull(executionTime, "Execution time must be not null");
        Objects.requireNonNull(task, "Task must be not null");
        Promise<T> promise = new Promise<>();
        if (isInterrupted()) {
            promise.reject();
        } else {
            queue.submit(new ClockTask<>(executionTime, task, promise));
        }
        return promise;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ClockTask task = queue.getTask();
                executeEvent(task);
            } catch (InterruptedException e) {
                queue.rejectAll();
                return;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void executeEvent(ClockTask task) {
        try {
            Object result = task.event.call();
            task.promise.success(result);
        } catch (InterruptedException e) {
            task.promise.reject();
        } catch (Exception e) {
            task.promise.failure(e);
        }
    }
}

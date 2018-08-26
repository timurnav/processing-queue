package com.developer.timurnav.executor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

public class SequentialEventsExecutor extends Thread {

    private final PriorityQueue<ClockTask> queue = new PriorityQueue<>();

    public <T> Promise<T> submit(LocalDateTime executionTime, Callable<T> event) {
        Objects.requireNonNull(executionTime, "Execution time must be not null");
        Objects.requireNonNull(event, "Event must be not null");
        synchronized (queue) {
            long executeAfter = executionTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1;
            Promise<T> promise = new Promise<>();
            queue.add(new ClockTask<>(executeAfter, event, promise));
            queue.notify();
            return promise;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        while (true) {
            try {
                ClockTask task = getTask();
                try {
                    Object result = task.event.call(); //todo run async
                    task.promise.success(result);
                } catch (Exception e) {
                    task.promise.failure(e);
                }
            } catch (InterruptedException e) {
                return; //todo do something with tasks in queue
            }
        }
    }

    private ClockTask getTask() throws InterruptedException {
        synchronized (queue) {
            while (true) {
                long timeToWait = Optional.ofNullable(queue.peek())
                    .map(task -> task.executionTime - System.currentTimeMillis())
                    .orElse(Long.MAX_VALUE);
                if (timeToWait <= 0) {
                    break;
                }
                queue.wait(timeToWait);
            }
            return Objects.requireNonNull(queue.poll(), "something went wrong");
        }
    }

}

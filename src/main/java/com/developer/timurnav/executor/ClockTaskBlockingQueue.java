package com.developer.timurnav.executor;

import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

public class ClockTaskBlockingQueue {

    private final PriorityQueue<ClockTask> queue = new PriorityQueue<>();
    private final int capacity;

    public ClockTaskBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    public ClockTaskBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void submit(ClockTask<?> task) {
        Objects.requireNonNull(task, "Task must be not null");
        synchronized (queue) {
            if (queue.size() == capacity) {
                task.promise.reject();
            } else {
                queue.add(task);
                queue.notify();
            }
        }
    }

    ClockTask getTask() throws InterruptedException {
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

    void rejectAll() {
        synchronized (queue) {
            queue.forEach(task -> task.promise.reject());
            queue.clear();
        }
    }
}

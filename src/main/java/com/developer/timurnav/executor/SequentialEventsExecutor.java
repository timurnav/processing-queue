package com.developer.timurnav.executor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;

public class SequentialEventsExecutor extends Thread {

    private final PriorityQueue<ClockTask> queue = new PriorityQueue<>();

    public void submit(LocalDateTime executionTime, Runnable event) {
        Objects.requireNonNull(executionTime, "Execution time must be not null");
        Objects.requireNonNull(event, "Event must be not null");
        synchronized (queue) {
            queue.add(new ClockTask(executionTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), event));
            queue.notify();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                getEvent().run(); //todo run async
            } catch (InterruptedException e) {
                return; //todo do something with tasks in queue
            } catch (Exception e) {
                e.printStackTrace(); //todo process exception properly
            }
        }
    }

    private Runnable getEvent() throws InterruptedException {
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
            return Objects.requireNonNull(queue.poll(), "something went wrong").event;
        }
    }

    private class ClockTask implements Comparable<ClockTask> {

        private final long executionTime;
        private final Runnable event;

        private ClockTask(long executionTime, Runnable event) {
            this.executionTime = executionTime;
            this.event = event;
        }

        @Override
        public int compareTo(ClockTask o) {
            if (this.executionTime == o.executionTime) {
                return 0;
            }
            return this.executionTime > o.executionTime ? 1 : -1;
        }
    }
}

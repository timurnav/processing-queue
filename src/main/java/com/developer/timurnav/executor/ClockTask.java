package com.developer.timurnav.executor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Callable;

class ClockTask<T> implements Comparable<ClockTask<T>> {

    final long executionTime;
    final Callable<T> event;
    final Promise<T> promise;
    private final LocalDateTime created;

    ClockTask(LocalDateTime executionTime, Callable<T> event, Promise<T> promise) {
        this.created = LocalDateTime.now();
        this.executionTime = executionTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() + 1;
        this.event = event;
        this.promise = promise;
    }

    @Override
    public int compareTo(ClockTask o) {
        if (this.executionTime == o.executionTime) {
            return this.created.compareTo(o.created);
        }
        return this.executionTime > o.executionTime ? 1 : -1;
    }

    @Override
    public String toString() {
        return "ClockTask{" +
            "executionTime=" + executionTime +
            ", created=" + created +
            '}';
    }
}
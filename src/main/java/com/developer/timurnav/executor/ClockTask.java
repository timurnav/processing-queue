package com.developer.timurnav.executor;

import java.util.concurrent.Callable;

class ClockTask<T> implements Comparable<ClockTask<T>> {

    final long executionTime;
    final Callable<T> event;
    final Promise<T> promise;

    ClockTask(long executionTime, Callable<T> event, Promise<T> promise) {
        this.executionTime = executionTime;
        this.event = event;
        this.promise = promise;
    }

    @Override
    public int compareTo(ClockTask o) {
        if (this.executionTime == o.executionTime) {
            return 0;
        }
        return this.executionTime > o.executionTime ? 1 : -1;
    }
}
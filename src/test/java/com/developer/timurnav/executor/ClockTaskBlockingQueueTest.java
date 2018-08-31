package com.developer.timurnav.executor;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ClockTaskBlockingQueueTest {

    @Test
    public void queueRejectsTasksOnCapacityOverflow() {
        ClockTaskBlockingQueue queue = new ClockTaskBlockingQueue(1);

        Runnable firstIsRejected = mock(Runnable.class);
        Runnable secondIsRejected = mock(Runnable.class);

        Promise<Object> firstPromise = new Promise<>();
        firstPromise.onReject(firstIsRejected);
        queue.submit(new ClockTask<>(LocalDateTime.now(), nullEvent(), firstPromise));

        Promise<Object> secondPromise = new Promise<>();
        secondPromise.onReject(secondIsRejected);
        queue.submit(new ClockTask<>(LocalDateTime.now(), nullEvent(), secondPromise));

        verify(firstIsRejected, never()).run();
        verify(secondIsRejected).run();
    }

    @Test
    public void queueReturnsTasksWithEarliestExecutionTimeAtFirst() throws Exception {
        ClockTaskBlockingQueue queue = new ClockTaskBlockingQueue();
        LocalDateTime now = LocalDateTime.now();

        ClockTask<Object> task1 = createTask(now.minusSeconds(2));
        ClockTask<Object> task2 = createTask(now);
        queue.submit(task2);
        queue.submit(task1);

        Assert.assertEquals(task1, queue.getTask());
        Assert.assertEquals(task2, queue.getTask());
    }

    @Test
    public void queueReturnsTasksWithSameExecutionTimeAtFIFO() throws Exception {
        ClockTaskBlockingQueue queue = new ClockTaskBlockingQueue();
        LocalDateTime now = LocalDateTime.now();

        ClockTask<Object> task1 = createTask(now);
        ClockTask<Object> task2 = createTask(now);

        for (int i = 0; i < 50; i++) {
            queue.submit(createTask(now.minusSeconds(10)));
        }
        queue.submit(task1);
        for (int i = 0; i < 50; i++) {
            queue.submit(createTask(now.minusSeconds(10)));
        }
        queue.submit(task2);
        for (int i = 0; i < 50; i++) {
            queue.submit(createTask(now.minusSeconds(10)));
        }

        for (int i = 0; i < 150; i++) {
            queue.getTask();
        }
        Assert.assertEquals(task1, queue.getTask());
        Assert.assertEquals(task2, queue.getTask());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void rejectAllTaskInQueue() {

        Consumer<Object> successHandler = (Consumer<Object>) mock(Consumer.class);
        Consumer<Exception> errorHandler = (Consumer<Exception>) mock(Consumer.class);
        Runnable rejectHandler = mock(Runnable.class);

        ClockTaskBlockingQueue queue = new ClockTaskBlockingQueue();

        Promise<Object> promise1 = new Promise<>();
        Promise<Object> promise2 = new Promise<>();
        queue.submit(new ClockTask<>(LocalDateTime.now(), nullEvent(), promise1));
        queue.submit(new ClockTask<>(LocalDateTime.now(), nullEvent(), promise2));

        queue.rejectAll();

        promise1
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        promise2
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        verify(successHandler, never()).accept(any());
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler, times(3)).run();

    }

    private ClockTask<Object> createTask(LocalDateTime now) {
        return new ClockTask<>(now, nullEvent(), new Promise<>());
    }

    private Callable<Object> nullEvent() {
        return () -> null;
    }
}
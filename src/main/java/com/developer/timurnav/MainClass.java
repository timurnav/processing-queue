package com.developer.timurnav;

import com.developer.timurnav.executor.SequentialEventsExecutor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

public class MainClass {

    public static void main(String[] args) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(23);

        SequentialEventsExecutor executor = new SequentialEventsExecutor();
        LocalDateTime now = LocalDateTime.now();
        executor.start();

        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 14, 5500);
        register(executor, now, startLatch, finishLatch, 9, 5000);
        register(executor, now, startLatch, finishLatch, 1, 1000);
        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 5, 2400);
        register(executor, now, startLatch, finishLatch, 6, 2500);
        register(executor, now, startLatch, finishLatch, 7, 3000);
        register(executor, now, startLatch, finishLatch, 13, 5400);
        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 8, 4000);
        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 11, 5200);
        register(executor, now, startLatch, finishLatch, 2, 2100);
        register(executor, now, startLatch, finishLatch, 7, 3000);
        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 3, 2200);
        register(executor, now, startLatch, finishLatch, 15, 6000);
        register(executor, now, startLatch, finishLatch, 10, 5100);
        register(executor, now, startLatch, finishLatch, 12, 5300);
        register(executor, now, startLatch, finishLatch, 1, 1000);
        register(executor, now, startLatch, finishLatch, 4, 2300);
        register(executor, now, startLatch, finishLatch, -1, -1000);

        startLatch.countDown();
        finishLatch.await();
        executor.interrupt();
    }

    private static void register(SequentialEventsExecutor executor, LocalDateTime now, CountDownLatch startLatch, CountDownLatch finishLatch, int number, int delayMillis) {
        CompletableFuture.runAsync(() -> {
            try {
                LocalDateTime executionTime = now.plus(delayMillis, ChronoUnit.MILLIS);
                startLatch.await();
                executor.submit(executionTime, () -> {
                    System.out.println(number);
                    finishLatch.countDown();
                });
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

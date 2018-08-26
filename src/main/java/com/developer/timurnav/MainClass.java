package com.developer.timurnav;

import com.developer.timurnav.executor.Promise;
import com.developer.timurnav.executor.SequentialEventsExecutor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class MainClass {

    private static final List<Integer> executedNumbers = new CopyOnWriteArrayList<>();
    private static final List<String> failedNumbers = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(23);

        SequentialEventsExecutor executor = new SequentialEventsExecutor();
        LocalDateTime now = LocalDateTime.now();
        executor.start();

        register(executor, now, startLatch, finishLatch, 151, 6000);
        register(executor, now, startLatch, finishLatch, 14, 5500);
        register(executor, now, startLatch, finishLatch, 9, 5000);
        register(executor, now, startLatch, finishLatch, 11, 1000);
        register(executor, now, startLatch, finishLatch, 152, 6000);
        register(executor, now, startLatch, finishLatch, 5, 2400);
        register(executor, now, startLatch, finishLatch, 6, 2500);
        register(executor, now, startLatch, finishLatch, 7, 3000);
        register(executor, now, startLatch, finishLatch, 13, 5400);
        register(executor, now, startLatch, finishLatch, 153, 6000);
        register(executor, now, startLatch, finishLatch, 8, 4000);
        register(executor, now, startLatch, finishLatch, 154, 6000);
        register(executor, now, startLatch, finishLatch, 11, 5200);
        register(executor, now, startLatch, finishLatch, 2, 2100);
        register(executor, now, startLatch, finishLatch, 7, 3000);
        register(executor, now, startLatch, finishLatch, 155, 6000);
        register(executor, now, startLatch, finishLatch, 3, 2200);
        register(executor, now, startLatch, finishLatch, 156, 6000);
        register(executor, now, startLatch, finishLatch, 10, 5100);
        register(executor, now, startLatch, finishLatch, 12, 5300);
        register(executor, now, startLatch, finishLatch, 12, 1000);
        register(executor, now, startLatch, finishLatch, 4, 2300);
        register(executor, now, startLatch, finishLatch, -1, -1000);

        startLatch.countDown();
        finishLatch.await();
        executor.interrupt();

        System.out.println(executedNumbers);
        System.out.println(failedNumbers);
    }

    private static void register(SequentialEventsExecutor executor, LocalDateTime now, CountDownLatch startLatch, CountDownLatch finishLatch, int number, int delayMillis) {
        CompletableFuture.runAsync(() -> {
            try {
                LocalDateTime executionTime = now.plus(delayMillis, ChronoUnit.MILLIS);
                startLatch.await();
                Promise<Integer> differedResult = executor.submit(executionTime, () -> {
                    finishLatch.countDown();
                    throw new NullPointerException("" + number);
//                    return number;
                });
                differedResult
                    .onSuccess(executedNumbers::add)
                    .onError(x -> failedNumbers.add(x.getMessage()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

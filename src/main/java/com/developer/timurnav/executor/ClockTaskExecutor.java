package com.developer.timurnav.executor;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public interface ClockTaskExecutor {

    <T> Promise<T> submit(LocalDateTime executionTime, Callable<T> task);

}

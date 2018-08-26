package com.developer.timurnav.executor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Promise<T> {

    private final Object lock = new Object();

    private Consumer<T> onSuccessConsumer;
    private Consumer<Exception> onErrorConsumer;
    private Supplier<T> valueSupplier;
    private Supplier<Exception> exceptionSupplier;
    private boolean executed = false;

    public Promise<T> onSuccess(Consumer<T> onSuccessConsumer) {
        synchronized (lock) {
            checkArgument(onSuccessConsumer != null, "consumer must be not null");
            checkArgument(this.onSuccessConsumer == null, "success consumer must be null");
            this.onSuccessConsumer = onSuccessConsumer;
            doExecuteEvent();
        }
        return this;
    }

    public Promise<T> onError(Consumer<Exception> onErrorConsumer) {
        synchronized (lock) {
            checkArgument(onErrorConsumer != null, "consumer must be not null");
            checkArgument(this.onErrorConsumer == null, "error consumer must be null");
            this.onErrorConsumer = onErrorConsumer;
            doExecuteEvent();
        }
        return this;
    }

    void success(T value) {
        synchronized (lock) {
            checkArgument(!executed, "event is already executed");
            this.valueSupplier = () -> value;
            doExecuteEvent();
        }
    }

    void failure(Exception exception) {
        synchronized (lock) {
            checkArgument(!executed, "event is already executed");
            this.exceptionSupplier = () -> exception;
            doExecuteEvent();
        }
    }

    private void doExecuteEvent() {
        if (executed) {
            return;
        }
        if (valueSupplier != null && onSuccessConsumer != null) {
            onSuccessConsumer.accept(valueSupplier.get());
            this.executed = true;
        } else if (exceptionSupplier != null && onErrorConsumer != null) {
            onErrorConsumer.accept(exceptionSupplier.get());
            this.executed = true;
        }
    }

    private void checkArgument(boolean argument, String errorMessage) {
        if (!argument) {
            throw new IllegalStateException(errorMessage);
        }
    }
}

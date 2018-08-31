package com.developer.timurnav.executor;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Promise<T> {

    private final Object lock = new Object();

    private Consumer<T> onSuccessHandler;
    private Consumer<Exception> onErrorHandler;
    private Runnable rejectHandler;

    private Supplier<T> valueSupplier;
    private Supplier<Exception> exceptionSupplier;

    private boolean executed = false;
    private boolean rejected = false;

    public Promise<T> onSuccess(Consumer<T> onSuccessConsumer) {
        synchronized (lock) {
            checkArgument(onSuccessConsumer != null, "consumer must be not null");
            checkArgument(this.onSuccessHandler == null, "success consumer must be null");
            this.onSuccessHandler = onSuccessConsumer;
            doExecuteEvent();
        }
        return this;
    }

    public Promise<T> onError(Consumer<Exception> onErrorConsumer) {
        synchronized (lock) {
            checkArgument(onErrorConsumer != null, "consumer must be not null");
            checkArgument(this.onErrorHandler == null, "error consumer must be null");
            this.onErrorHandler = onErrorConsumer;
            doExecuteEvent();
        }
        return this;
    }

    public void onReject(Runnable runnable) {
        synchronized (lock) {
            checkArgument(runnable != null, "consumer must be not null");
            checkArgument(this.rejectHandler == null, "error consumer must be null");
            this.rejectHandler = runnable;
            doExecuteEvent();
        }
    }

    void success(T value) {
        synchronized (lock) {
            checkArgument(!executed, "event is already executed");
            checkArgument(!rejected, "event is already rejected");
            this.valueSupplier = () -> value;
            doExecuteEvent();
        }
    }

    void failure(Exception exception) {
        synchronized (lock) {
            checkArgument(!executed, "event is already executed");
            checkArgument(!rejected, "event is already rejected");
            this.exceptionSupplier = () -> exception;
            doExecuteEvent();
        }
    }

    void reject() {
        synchronized (lock) {
            if (executed) {
                return;
            }
            checkArgument(!rejected, "event is already rejected");
            rejected = true;
            doExecuteEvent();
        }
    }

    private void doExecuteEvent() {
        if (executed) {
            return;
        }
        if (valueSupplier != null && onSuccessHandler != null) {
            onSuccessHandler.accept(valueSupplier.get());
            this.executed = true;
        } else if (exceptionSupplier != null && onErrorHandler != null) {
            onErrorHandler.accept(exceptionSupplier.get());
            this.executed = true;
        } else if (rejected && rejectHandler != null) {
            rejectHandler.run();
        }
    }

    private void checkArgument(boolean argument, String errorMessage) {
        if (!argument) {
            throw new IllegalStateException(errorMessage);
        }
    }
}

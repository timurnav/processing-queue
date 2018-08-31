package com.developer.timurnav.executor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Consumer;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PromiseTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Consumer<Object> successHandler;
    private Consumer<Exception> errorHandler;
    private Runnable rejectHandler;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        successHandler = (Consumer<Object>) mock(Consumer.class);
        errorHandler = (Consumer<Exception>) mock(Consumer.class);
        rejectHandler = mock(Runnable.class);
    }

    @Test
    public void onJustCreated() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        verify(successHandler, never()).accept(any());
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler, never()).run();
    }

    @Test
    public void onSuccessBeforeEvent() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        Object testData = new Object();
        promise.success(testData);
        verify(successHandler).accept(testData);
        verify(errorHandler, never()).accept(any(Exception.class));
        verify(rejectHandler, never()).run();
    }

    @Test
    public void onSuccessAfterEvent() {
        Promise<Object> promise = new Promise<>();
        Object testData = new Object();
        promise.success(testData);
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        verify(successHandler).accept(testData);
        verify(errorHandler, never()).accept(any(Exception.class));
        verify(rejectHandler, never()).run();
    }

    @Test
    public void onErrorBeforeEvent() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        Exception exception = new Exception();
        promise.failure(exception);
        verify(successHandler, never()).accept(any());
        verify(errorHandler).accept(exception);
        verify(rejectHandler, never()).run();
    }

    @Test
    public void onErrorAfterEvent() {
        Promise<Object> promise = new Promise<>();
        Exception exception = new Exception();
        promise.failure(exception);
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        verify(successHandler, never()).accept(any());
        verify(errorHandler).accept(exception);
        verify(rejectHandler, never()).run();
    }

    @Test
    public void onRejectBeforeEvent() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        promise.reject();
        verify(successHandler, never()).accept(any());
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler).run();
    }

    @Test
    public void onRejectAfterEvent() {
        Promise<Object> promise = new Promise<>();
        promise.reject();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        verify(successHandler, never()).accept(any());
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler).run();
    }

    @Test
    public void successAndThanReject() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        Object testData = new Object();
        promise.success(testData);
        promise.reject();
        verify(successHandler).accept(testData);
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler, never()).run();
    }

    @Test
    public void exceptionOnTwoEvents() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        Object testData = new Object();
        promise.success(testData);
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("event is already executed");
        promise.failure(new Exception());
        verify(successHandler).accept(testData);
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler, never()).run();
    }

    @Test
    public void rejectAndThanSuccess() {
        Promise<Object> promise = new Promise<>();
        promise
            .onSuccess(successHandler)
            .onError(errorHandler)
            .onReject(rejectHandler);
        promise.reject();
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("event is already rejected");
        promise.success(new Object());
        verify(successHandler, never()).accept(any());
        verify(errorHandler, never()).accept(any());
        verify(rejectHandler).run();
    }
}
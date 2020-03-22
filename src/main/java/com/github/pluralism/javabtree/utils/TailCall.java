package com.github.pluralism.javabtree.utils;

@FunctionalInterface
public interface TailCall<T> {
    TailCall<T> apply();

    default boolean isCompleted() {
        return false;
    }

    default T result() {
        throw new UnsupportedOperationException();
    }

    default T run() {
        TailCall<T> currentCall = this;

        while (!currentCall.isCompleted()) {
            currentCall = currentCall.apply();
        }

        return currentCall.result();
    }
}

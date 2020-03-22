package com.github.pluralism.javabtree.utils;

public class TailCalls {
    public static <T> TailCall<T> done(final T result) {
        return new TailCall<T>() {
            @Override
            public boolean isCompleted() {
                return true;
            }

            @Override
            public T result() {
                return result;
            }

            @Override
            public TailCall<T> apply() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

package com.github.pluralism.javabtree;

import java.util.Optional;

public interface BTree<T extends Comparable<T>> {
    void insert(T k);

    void delete(T k);

    boolean contains(T k);

    Optional<T> get(T k);

    T getMinValue();

    T getMaxValue();
}

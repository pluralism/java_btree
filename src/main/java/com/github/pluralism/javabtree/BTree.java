package com.github.pluralism.javabtree;

import java.util.Optional;

/**
 * Generic interface used to interact with an implementation of a B-Tree.
 *
 * @param <T> type parameter of items to be used in the B-Tree.
 */
public interface BTree<T extends Comparable<T>> {
    /**
     * Inserts an item on the B-Tree.
     *
     * @param k value to be inserted
     */
    void insert(T k);

    /**
     * Deletes an item from the B-Tree.
     *
     * @param k value to be removed
     */
    void delete(T k);

    /**
     * Checks if the B-Tree contains a given item.
     *
     * @param k value to be checked
     * @return true if the item exists on the B-Tree, false otherwise
     */
    boolean contains(T k);

    /**
     * Returns the object in the B-Tree associated with a given item.
     * The {@link Comparable#compareTo(Object)} method is used to check if a given item is equal to the item in the
     * B-Tree. It is the responsibility of the user to provide a proper {@link Comparable#compareTo(Object)}
     * implementation.
     *
     * @param k value to be extracted from the B-Tree
     * @return {@link Optional#empty()} if the value is not on the B-Tree, otherwise returns an {@link Optional}
     * with the value from the B-Tree.
     */
    Optional<T> get(T k);

    /**
     * Get the minimum value in the B-Tree according to the {@link Comparable#compareTo(Object)} implementation.
     *
     * @return minimum value in the B-Tree.
     */
    T getMinValue();

    /**
     * Get the maximum value in the B-Tree according to the {@link Comparable#compareTo(Object)} implementation.
     *
     * @return maximum value in the B-Tree.
     */
    T getMaxValue();
}

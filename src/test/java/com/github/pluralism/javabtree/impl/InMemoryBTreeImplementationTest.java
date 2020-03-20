package com.github.pluralism.javabtree.impl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryBTreeImplementationTest {
    @Test
    void containsShouldReturnTrueOnExistingValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        tree.insert(1);

        assertTrue(tree.contains(1));
    }

    @Test
    void containsShouldReturnFalseOnNonExistingValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        tree.insert(1);

        assertFalse(tree.contains(2));
    }

    @Test
    void getMinValueShouldReturnTheMinimumValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(1, tree.getMinValue());
    }

    @Test
    void getMaxValueShouldReturnTheMaximumValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(10, tree.getMaxValue());
    }

    @Test
    void getShouldReturnEmptyOptionalOnNonExistingValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(Optional.empty(), tree.get(100));
    }

    @Test
    void getShouldReturnNonEmptyOptionalOnExistingValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertTrue(tree.get(1).isPresent());
    }

    @Test
    void deleteShouldRemoveValueFromTree() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        tree.delete(2);

        assertFalse(tree.contains(2));
    }
}

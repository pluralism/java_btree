package com.github.pluralism.javabtree.impl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

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
    void getMinShouldReturnTheMinimumValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final Optional<Integer> min = tree.getMin();
        assertTrue(min.isPresent());
        assertEquals(1, min.get());
    }

    @Test
    void getMinOnEmptyTreeShouldReturnEmptyOptional() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        assertEquals(Optional.empty(), tree.getMin());
    }

    @Test
    void getMaxShouldReturnTheMaximumValue() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final Optional<Integer> max = tree.getMax();
        assertTrue(max.isPresent());
        assertEquals(10, max.get());
    }

    @Test
    void getMaxOnEmptyTreeShouldReturnEmptyOptional() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        assertEquals(Optional.empty(), tree.getMax());
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

    @Test
    void deleteFromEmptyTreeShouldNotThrowExceptions() {
        final InMemoryBTree.Implementation<Integer> tree = new InMemoryBTree.Implementation<>(2);
        assertDoesNotThrow(() -> tree.delete(2));
    }
}

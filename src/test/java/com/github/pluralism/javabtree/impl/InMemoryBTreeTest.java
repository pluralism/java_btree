package com.github.pluralism.javabtree.impl;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryBTreeTest {
    @Test
    void constructorShouldThrowOnInvalidArgument() {
        assertThrows(IllegalArgumentException.class, () -> new InMemoryBTree<Integer>(1));
    }

    @Test
    void constructorShouldInitializeRootOnValidArgument() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        final InMemoryBTree<Integer>.BTreeNode root = tree.getRoot();

        assertEquals(0, root.getN());
        assertTrue(root.isLeaf());
        assertEquals(3, root.getKeys().size());
        assertEquals(4, root.getChildren().size());

        assertTrue(root.getKeys().stream().allMatch(Objects::isNull));
        assertTrue(root.getChildren().stream().allMatch(Objects::isNull));
    }

    @Test
    void insertOneItemShouldNotMakeRootFull() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);

        assertFalse(tree.getRoot().isFull());
    }

     @Test
    void insertSingleItemShouldFillFirstKeyPosition() {
         final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
         tree.insert(1);

         assertEquals(1, tree.getRoot().getKeys().get(0).getKey());
    }

    @Test
    void insertThreeItemsShouldMakeRootFull() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);

        assertTrue(tree.getRoot().isFull());
    }

    @Test
    void insertThreeItemsShouldFillAllNodeKeys() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);

        for (int i = 0; i < tree.getRoot().getKeys().size(); i++) {
            assertEquals(i + 1, tree.getRoot().getKeys().get(i).getKey());
        }
    }

    @Test
    void insertThreeItemsShouldNotCreateChildrenNodes() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);

        for (int i = 0; i < tree.getRoot().getChildren().size(); i++) {
            assertNull(tree.getRoot().getChildren().get(i));
        }
    }

    @Test
    void insertFourItemsShouldSplitRoot() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.insert(4);

        assertFalse(tree.getRoot().isFull());
        assertFalse(tree.getRoot().isLeaf());
    }

    @Test
    void insertFourItemsShouldMakeRootHaveTwoChildren() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.insert(4);

        final long notNullChildrenCount = tree.getRoot().getChildren().stream().filter(Objects::nonNull).count();
        assertEquals(2, notNullChildrenCount);

        final InMemoryBTree<Integer>.BTreeNode firstChild = tree.getRoot().getChildren().get(0);
        assertFalse(firstChild.isFull());
        assertTrue(firstChild.isLeaf());
        assertEquals(1, firstChild.getN());
        assertEquals(1, firstChild.getKeys().get(0).getKey());

        final InMemoryBTree<Integer>.BTreeNode secondChild = tree.getRoot().getChildren().get(1);
        assertFalse(firstChild.isFull());
        assertTrue(firstChild.isLeaf());
        assertEquals(2, secondChild.getN());
        assertEquals(3, secondChild.getKeys().get(0).getKey());
        assertEquals(4, secondChild.getKeys().get(1).getKey());
    }

    @Test
    void insertTenItemsShouldMakeTreeWithThreeLevels() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final InMemoryBTree<Integer>.BTreeNode firstRootChild = tree.getRoot().getChildren().get(0);
        final InMemoryBTree<Integer>.BTreeNode secondRootChild = tree.getRoot().getChildren().get(1);

        assertFalse(firstRootChild.isLeaf());
        assertFalse(secondRootChild.isLeaf());

        assertEquals(2, firstRootChild.getChildren().stream().filter(Objects::nonNull).count());
        assertEquals(3, secondRootChild.getChildren().stream().filter(Objects::nonNull).count());
    }

    @Test
    void rootChildrenShouldHaveTheCorrectValuesWhenGivenTreeWithTenItems() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final InMemoryBTree<Integer>.BTreeNode firstRootChild = tree.getRoot().getChildren().get(0);
        final InMemoryBTree<Integer>.BTreeNode secondRootChild = tree.getRoot().getChildren().get(1);

        assertEquals(2, firstRootChild.getKeys().get(0).getKey());
        assertEquals(6, secondRootChild.getKeys().get(0).getKey());
        assertEquals(8, secondRootChild.getKeys().get(1).getKey());
    }

    @Test
    void rootGrandchildrenShouldBeLeafsWhenGivenTreeWithTenItems() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final InMemoryBTree<Integer>.BTreeNode firstRootChild = tree.getRoot().getChildren().get(0);
        final InMemoryBTree<Integer>.BTreeNode secondRootChild = tree.getRoot().getChildren().get(1);

        for (int i = 0; i < firstRootChild.getChildren().size(); i++) {
            final InMemoryBTree<Integer>.BTreeNode grandchildren = firstRootChild.getChildren().get(i);
            if (grandchildren != null) {
                assertTrue(grandchildren.isLeaf());
            }
        }

        for (int i = 0; i < secondRootChild.getChildren().size(); i++) {
            final InMemoryBTree<Integer>.BTreeNode grandchildren = secondRootChild.getChildren().get(i);
            if (grandchildren != null) {
                assertTrue(grandchildren.isLeaf());
            }
        }
    }

    @Test
    void rootGrandchildrenShouldHaveTheCorrectValuesWhenGivenTreeWithTenItems() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final InMemoryBTree<Integer>.BTreeNode firstRootChild = tree.getRoot().getChildren().get(0);
        final InMemoryBTree<Integer>.BTreeNode secondRootChild = tree.getRoot().getChildren().get(1);

        assertEquals(1, firstRootChild.getChildren().get(0).getKeys().get(0).getKey());
        assertEquals(3, firstRootChild.getChildren().get(1).getKeys().get(0).getKey());

        assertEquals(5, secondRootChild.getChildren().get(0).getKeys().get(0).getKey());
        assertEquals(7, secondRootChild.getChildren().get(1).getKeys().get(0).getKey());
        assertEquals(9, secondRootChild.getChildren().get(2).getKeys().get(0).getKey());
        assertEquals(10, secondRootChild.getChildren().get(2).getKeys().get(1).getKey());
    }

    @Test
    void containsShouldReturnTrueWhenGivenAnExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertTrue(tree.contains(1));
        assertTrue(tree.contains(10));
    }

    @Test
    void containsShouldReturnFalseWhenGivenAnNonExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertFalse(tree.contains(0));
        assertFalse(tree.contains(10000));
    }

    @Test
    void getMinValueShouldReturnTheMinimumValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(1, tree.getMinValue());
    }

    @Test
    void getMaxValueShouldReturnTheMaximumValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(10, tree.getMaxValue());
    }

    @Test
    void deleteValueFromLeafNodeWithTwoKeys() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.insert(4);

        tree.delete(4);

        assertFalse(tree.contains(4));
        assertEquals(1, tree.getRoot().getChildren().get(1).getN());
    }

    @Test
    void deleteValueFromRootWithSingleElementShouldCreateNewRoot() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.insert(4);

        assertEquals(2, tree.getRoot().getKeys().get(0).getKey());
        tree.delete(2);
        assertEquals(1, tree.getRoot().getN());
        assertEquals(3, tree.getRoot().getKeys().get(0).getKey());
    }

    @Test
    void deleteNodeWithSingleValueShouldMergeWithNextNode() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        tree.delete(2);

        final InMemoryBTree<Integer>.BTreeNode firstChildNode = tree.getRoot().getChildren().get(0);

        assertEquals(2, firstChildNode.getChildren().get(0).getN());
        assertEquals(1, firstChildNode.getChildren().get(0).getKeys().get(0).getKey());
        assertEquals(3, firstChildNode.getChildren().get(0).getKeys().get(1).getKey());
    }

    @Test
    void deleteNodeWithSingleValueShouldMergeWithPreviousNode() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        tree.insert(1);
        tree.insert(2);
        tree.insert(3);
        tree.insert(1);
        tree.insert(1);

        tree.delete(3);

        assertEquals(1, tree.getRoot().getN());
        assertEquals(1, tree.getRoot().getKeys().get(0).getKey());

        final InMemoryBTree<Integer>.BTreeNode rootFirstChild = tree.getRoot().getChildren().get(0);
        assertEquals(2, rootFirstChild.getN());
        assertTrue(IntStream.range(0, rootFirstChild.getN())
                .mapToObj(i -> rootFirstChild.getKeys().get(i).getKey()).allMatch(v -> v.equals(1)));
    }

    @Test
    void getShouldReturnEmptyOptionalOnNonExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(Optional.empty(), tree.get(100));
    }

    @Test
    void getShouldReturnNonEmptyOptionalOnExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertTrue(tree.get(1).isPresent());
    }

    @Test
    void searchNodeShouldReturnEmptyOptionalOnNonExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        assertEquals(Optional.empty(), tree.searchNode(100));
    }

    @Test
    void searchNodeShouldReturnNonEmptyOptionalOnExistingValue() {
        final InMemoryBTree<Integer> tree = new InMemoryBTree<>(2);
        IntStream.range(1, 11).forEach(tree::insert);

        final Optional<InMemoryBTree<Integer>.SearchNodeResult> searchNodeResultOptional = tree.searchNode(6);
        assertTrue(searchNodeResultOptional.isPresent());

        final InMemoryBTree<Integer>.SearchNodeResult searchNodeResult = searchNodeResultOptional.get();
        assertEquals(0, searchNodeResult.getIndex());

        final InMemoryBTree<Integer>.BTreeNode node = searchNodeResult.getNode();
        assertEquals(2, node.getN());
        assertEquals(6, node.getKeys().get(0).getKey());
        assertEquals(8, node.getKeys().get(1).getKey());
    }
}

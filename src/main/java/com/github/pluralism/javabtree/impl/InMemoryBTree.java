package com.github.pluralism.javabtree.impl;

import com.github.pluralism.javabtree.BTree;
import com.github.pluralism.javabtree.utils.TailCall;
import com.github.pluralism.javabtree.utils.TailCalls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * B-Tree is a self-balancing search tree.
 * Unlike a binary tree, each node in a B-Tree can have more than 2 children. The number of children depends on a
 * parameter T, that is referred as the tree minimum degree.
 * Most of the operations in a B-Tree, including searches, insertions, deletions can be completed in logarithmic time,
 * requiring O(h) disk accesses, where h is the height of the tree.
 * B-Tree are commonly used in database indexing.
 * <p>
 *  @see <a href="https://en.wikipedia.org/wiki/B-tree">B-tree (Wikipedia)</a>
 *  <br>
 *  @author André Pinheiro <andrepdpinheiro@gmail.com>
 * </p>
 */
public class InMemoryBTree<NodeType extends Comparable<NodeType>> {
    private BTreeNode root;

    /**
     * Minimum degree of the B-Tree (must be even and greater than or equal to 2).
     *
     * Every node other than the root must have at least T - 1 keys. Every internal node other than the root
     * must have at least T children.
     *
     * Every node may contain at most 2T - 1 keys. Therefore, an internal node may have at most 2T children.
     * A node is said to be full if it contains exactly 2T - 1 keys.
     */
    private int T;

    class BTreeNode {
        private boolean leaf;
        private int n;
        private List<BTreeNodeEntry> keys;
        private List<BTreeNode> children;

        int getN() {
            return n;
        }

        List<BTreeNodeEntry> getKeys() {
            return keys;
        }

        List<BTreeNode> getChildren() {
            return children;
        }

        boolean isLeaf() {
            return leaf;
        }

        boolean isFull() {
            return n == 2 * T - 1;
        }
    }

    class BTreeNodeEntry {
        private NodeType key;

        private BTreeNodeEntry(final NodeType key) {
            this.key = key;
        }

        int compareTo(final BTreeNodeEntry other) {
            return key.compareTo(other.key);
        }

        NodeType getKey() {
            return key;
        }
    }

    class SearchNodeResult {
        private BTreeNode node;
        private int index;

        SearchNodeResult(final BTreeNode node, final int index) {
            this.node = node;
            this.index = index;
        }

        BTreeNode getNode() {
            return node;
        }

        int getIndex() {
            return index;
        }
    }

    InMemoryBTree(final int T) {
        if (T < 2) {
            throw new IllegalArgumentException("Tree degree must be equal of greater than 2");
        }
        this.T = T;
        allocateRootNode();
    }

    BTreeNode getRoot() {
        return root;
    }

    private void allocateRootNode() {
        final BTreeNode node = allocateNode();
        node.leaf = true;
        node.n = 0;

        this.root = node;
    }

    private BTreeNode allocateNode() {
        final BTreeNode node = new BTreeNode();
        node.leaf = false;
        node.n = 0;
        node.keys = new ArrayList<>();
        node.children = new ArrayList<>();

        node.keys.addAll(Collections.nCopies(2 * T - 1, null));
        node.children.addAll(Collections.nCopies(2 * T, null));

        return node;
    }

    public void delete(final NodeType k) {
        if (contains(k)) {
            delete(root, new BTreeNodeEntry(k)).run();
        }
    }

    private TailCall<Void> delete(final BTreeNode x, final BTreeNodeEntry k) {
        return () -> {
            if (x.leaf) {
                deleteFromLeafNode(x, k);
                return TailCalls.done(null);
            }

            int i = 0;
            while (i < x.n && x.keys.get(i).compareTo(k) < 0) {
                i++;
            }

            // The key "k" is in node "x" and "x" is an internal node (non-leaf node).
            if (i < x.n && x.keys.get(i).compareTo(k) == 0) {
                if (x.children.get(i).n >= T) {
                    // Find the predecessor "k'" of "k" in the subtree rooted at "y" (x.children[i])
                    final BTreeNodeEntry predecessor = findPredecessor(x.children.get(i));
                    x.keys.set(i, predecessor); // replace "k" by "k'"
                    return delete(x.children.get(i), predecessor); // recursively delete "k'"
                }
                /*
                 * If "y" (x.children[i]) has fewer than T keys, examine the child "z" (x.children[i + 1]) that follows
                 * "k" (x.key[i]) in node "x".
                 * If "z" (x.children[i + 1]) has at least T keys, then find the successor "k'" of "k" in the subtree
                 * rooted at "z" (x.children[i + 1]). Recursively delete "k'", and replace k by "k'" in "x".
                 */
                else if (x.children.get(i + 1).n >= T) {
                    final BTreeNodeEntry successor = findSuccessor(x.children.get(i + 1));
                    x.keys.set(i, successor);
                    return delete(x.children.get(i + 1), successor);
                }
                /*
                 * Otherwise, if both "y" (x.children[i]) and "z" (x.children[i + 1]) have less than T keys, merge "k" and
                 * all of "z" (x.children[i + 1]) into "y" (x.children[i]), so that "x" loses both "k" and
                 * the pointer to "z" (x.children[i + 1]), and y (x.children[i]) now contains 2T - 1 keys.
                 * Finally free "z" (x.children[i + 1]) and recursively delete "k" from "y" (x.children[i]).
                 */
                else {
                    merge(x, i);
                    return delete(x.children.get(i), k);
                }
            }
            /*
             * If the key "k" is not present in internal node "x", determine the root x.children[i] of the appropriate
             * subtree that must contain "k". If x.children[i] has only T - 1 keys (minimum), we need to perform additional
             * steps to guarantee that we descend to a node containing at least T keys.
             */
            else {
                BTreeNode newChild = x.children.get(i);
                if (x.children.get(i).n < T) {
                    // Delete from a sibling if x.children[i] has only T - 1 keys.
                    newChild = deleteFromSibling(x, i);
                }

                // Finish by recursing on the appropriate child of "x"
                return delete(newChild, k);
            }
        };
    }

    private void deleteFromLeafNode(final BTreeNode x, final BTreeNodeEntry k) {
        for (int i = 0; i < x.n; i++) {
            if (x.keys.get(i).compareTo(k) == 0) {
                for (int j = i + 1; j < x.n; j++) {
                    x.keys.set(j - 1, x.keys.get(j));
                    x.keys.set(j, null);
                }

                x.n = x.n - 1;
                return;
            }
        }
    }

    private BTreeNode deleteFromSibling(final BTreeNode parentNode, int childIndex) {
        final BTreeNode child = parentNode.children.get(childIndex);

        if (childIndex > 0 && parentNode.children.get(childIndex - 1).n >= T) {
            stealNodeFromPreviousChild(parentNode, childIndex);
        } else if (childIndex != child.n && parentNode.children.get(childIndex + 1).n >= T) {
            stealNodeFromNextChild(parentNode, childIndex);
        } else {
            final boolean isLastChild = childIndex == child.n;
            if (isLastChild) {
                merge(parentNode, childIndex - 1);
                return parentNode.children.get(childIndex - 1);
            } else {
                merge(parentNode, childIndex);
            }
        }

        return child;
    }

    private void stealNodeFromPreviousChild(final BTreeNode parentNode, int childIndex) {
        final BTreeNode child = parentNode.children.get(childIndex);
        final BTreeNode previousChild = parentNode.children.get(childIndex - 1);

        for (int i = child.n - 1; i >= 0; i--) {
            child.keys.set(i + 1, child.keys.get(i));
        }

        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children.set(i + 1, child.children.get(i));
            }
        }

        child.keys.set(0, parentNode.keys.get(childIndex - 1));

        if (!child.leaf) {
            child.children.set(0, previousChild.children.get(previousChild.n));
        }

        parentNode.keys.set(childIndex - 1, previousChild.keys.get(previousChild.n - 1));
        previousChild.keys.set(previousChild.n - 1, null);

        child.n = child.n + 1;
        previousChild.n = previousChild.n - 1;
    }

    private void stealNodeFromNextChild(final BTreeNode parentNode, int childIndex) {
        final BTreeNode child = parentNode.children.get(childIndex);
        final BTreeNode nextChild = parentNode.children.get(childIndex + 1);

        child.keys.set(child.n, parentNode.keys.get(childIndex));

        if (!child.leaf) {
            child.children.set(child.n + 1, nextChild.children.get(0));
        }

        parentNode.keys.set(childIndex, nextChild.keys.get(0));

        for (int i = 0; i < nextChild.n; i++) {
            nextChild.keys.set(i, nextChild.keys.get(i + 1));
            nextChild.keys.set(i + 1, null);
        }

        if (!nextChild.leaf) {
            for (int i = 0; i <= nextChild.n; i++) {
                nextChild.children.set(i, nextChild.children.get(i + 1));
                nextChild.children.set(i + 1, null);
            }
        }

        child.n = child.n + 1;
        nextChild.n = nextChild.n - 1;
    }

    private void merge(final BTreeNode x, final int index) {
        final BTreeNode y = x.children.get(index);
        final BTreeNode z = x.children.get(index + 1);
        final BTreeNodeEntry k = x.keys.get(index);

        y.keys.set(T - 1, k);

        for (int i = 0; i < z.n; i++) {
            y.keys.set(i + T, z.keys.get(i));
        }

        if (!y.leaf) {
            for (int i = 0; i <= z.n; i++) {
                y.children.set(i + T, z.children.get(i));
            }
        }

        for (int i = index + 1; i < x.n; i++) {
            x.keys.set(i - 1, x.keys.get(i));
            x.keys.set(i, null);
        }

        for (int i = index + 2; i <= x.n; i++) {
            x.children.set(i - 1, x.children.get(i));
            x.children.set(i, null);
        }

        y.n = y.n + z.n + 1;
        x.n = x.n - 1;
    }

    private BTreeNodeEntry findPredecessor(final BTreeNode child) {
        BTreeNode current = child;
        while (!current.leaf) {
            current = current.children.get(child.n);
        }

        return current.keys.get(current.n - 1);
    }

    private BTreeNodeEntry findSuccessor(final BTreeNode child) {
        BTreeNode current = child;
        while (!current.leaf) {
            current = current.children.get(0);
        }

        return current.keys.get(0);
    }

    public boolean contains(final NodeType k) {
        return searchNode(k).isPresent();
    }

    public Optional<NodeType> get(NodeType k) {
        final Optional<SearchNodeResult> nodeOptional = searchNode(k);
        return nodeOptional.map(tSearchNodeResult -> tSearchNodeResult.node.keys.get(tSearchNodeResult.index).key);
    }

    public Optional<SearchNodeResult> searchNode(final NodeType k) {
        return searchNode(root, new BTreeNodeEntry(k)).run();
    }

    private TailCall<Optional<SearchNodeResult>> searchNode(final BTreeNode node, final BTreeNodeEntry k) {
        return () -> {
            int i = 0;
            while (i < node.n && k.compareTo(node.keys.get(i)) > 0) {
                i++;
            }

            if (i < node.n && k.compareTo(node.keys.get(i)) == 0) {
                return TailCalls.done(Optional.of(new SearchNodeResult(node, i)));
            } else if (node.leaf) {
                return TailCalls.done(Optional.empty());
            }

            return searchNode(node.children.get(i), k);
        };
    }

    public Optional<NodeType> getMin() {
        BTreeNode currentNode = root;

        while (!currentNode.leaf) {
            currentNode = currentNode.children.get(0);
        }

        return Optional.ofNullable(currentNode.keys.get(0)).map(v -> v.key);
    }

    public Optional<NodeType> getMax() {
        BTreeNode currentNode = root;

        while (!currentNode.leaf) {
            currentNode = currentNode.children.get(currentNode.n);
        }

        return Optional.ofNullable(currentNode.keys.get(Math.max(0, currentNode.n - 1))).map(v -> v.key);
    }

    public void insert(final NodeType k) {
        final BTreeNode root = this.root;
        final BTreeNodeEntry bTreeNodeEntry = new BTreeNodeEntry(k);

        if (root.isFull()) {
            final BTreeNode s = allocateNode();
            this.root = s;
            s.leaf = false;
            s.n = 0;
            s.children.set(0, root);

            splitChild(s, 0);
            insertNonFull(s, bTreeNodeEntry);
        } else {
            insertNonFull(root, bTreeNodeEntry);
        }
    }

    private void splitChild(final BTreeNode node, final int childIndex) {
        final BTreeNode z = allocateNode();
        final BTreeNode y = node.children.get(childIndex);

        z.leaf = y.leaf;
        z.n = T - 1;

        for (int j = 0; j < T - 1; j++) {
            z.keys.set(j, y.keys.get(j + T));
            y.keys.set(j + T, null);
        }

        if (!y.leaf) {
            for (int j = 0; j < T; j++) {
                z.children.set(j, y.children.get(j + T));
                y.children.set(j + T, null);
            }
        }

        y.n = T - 1;

        for (int j = node.n; j >= childIndex + 1; j--) {
            node.children.set(j + 1, node.children.get(j));
            node.children.set(j, null);
        }
        node.children.set(childIndex + 1, z);

        for (int j = node.n - 1; j >= childIndex; j--) {
            node.keys.set(j + 1, node.keys.get(j));
        }
        node.keys.set(childIndex, y.keys.get(T - 1));

        y.keys.set(T - 1, null);
        node.n = node.n + 1;
    }

    private void insertNonFull(final BTreeNode x, final BTreeNodeEntry k) {
        int i = x.n - 1;

        if (x.leaf) {
            while (i >= 0 && k.compareTo(x.keys.get(i)) < 0) {
                x.keys.set(i + 1, x.keys.get(i));
                i--;
            }

            x.keys.set(i + 1, k);
            x.n = x.n + 1;
        } else {
            while (i >= 0 && k.compareTo(x.keys.get(i)) < 0) {
                i--;
            }
            i++;

            if (x.children.get(i).isFull()) {
                splitChild(x, i);
                // Determine which of the two children is now the the correct one to descend to.
                if (k.compareTo(x.keys.get(i)) > 0) {
                    i++;
                }
            }

            insertNonFull(x.children.get(i), k);
        }
    }

    public static class Implementation<NodeType extends Comparable<NodeType>> implements BTree<NodeType> {
        final InMemoryBTree<NodeType> tree;

        public Implementation(final int T) {
            tree = new InMemoryBTree<>(T);
        }

        @Override
        public void insert(final NodeType k) {
            tree.insert(k);
        }

        @Override
        public void delete(final NodeType k) {
            tree.delete(k);
        }

        @Override
        public boolean contains(final NodeType k) {
            return tree.contains(k);
        }

        @Override
        public Optional<NodeType> get(final NodeType k) {
            return tree.get(k);
        }

        @Override
        public Optional<NodeType> getMin() {
            return tree.getMin();
        }

        @Override
        public Optional<NodeType> getMax() {
            return tree.getMax();
        }
    }
}
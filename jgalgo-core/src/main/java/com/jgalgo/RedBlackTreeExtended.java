/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

/**
 * A red black tree that support extensions such as subtree size/min/max.
 * <p>
 * Each node in the balanced binary tree can maintain properties such as its subtree size, or a reference to the
 * minimum/maximum element in its subtree. These properties can be updated during the regular operations of the red
 * black tree without increasing the asymptotical running time.
 * <p>
 * This red black tree implementation support arbitrary number of extensions, for example a size and max extensions are
 * used in this snippet:
 *
 * <pre> {@code
 * RedBlackTreeExtension.Size<Integer, String> sizeExt = new RedBlackTreeExtension.Size<>();
 * RedBlackTreeExtension.Max<Integer, String> maxExt = new RedBlackTreeExtension.Max<>();
 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(sizeExt, maxExt));
 *
 * HeapReference<Integer, String> e1 = tree.insert(15, "Alice");
 * tree.insert(5, "Bob");
 * tree.insert(3, "Charlie");
 * tree.insert(1, "Door");
 * ...
 * tree.insert(1, "Zebra");
 *
 * int subTreeSize = sizeExt.getSubTreeSize(e1);
 * HeapReference<Integer, String> subTreeMax = maxExt.getSubTreeMax(e1);
 * System.out.println("The subtree of " + e1 + " is of size " + subTreeSize);
 * System.out.println("The maximum element in the sub tree of " + e1 + " is " + subTreeMax);
 * }</pre>
 *
 * @param  <K> the keys type
 * @param  <V> the values type
 * @author Barak Ugav
 */
public class RedBlackTreeExtended<K, V> extends RedBlackTree<K, V> {

	private Node<K, V>[] nodes;
	private final RedBlackTreeExtension<K, V>[] extensions;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodesArray = new Node[0];

	/**
	 * Constructs a new, empty red black tree, with the given extensions, ordered according to the natural ordering of
	 * its keys.
	 * <p>
	 * All keys inserted into the tree must implement the {@link Comparable} interface. Furthermore, all such keys must
	 * be <i>mutually comparable</i>: {@code k1.compareTo(k2)} must not throw a {@code ClassCastException} for any keys
	 * {@code k1} and {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this
	 * constraint (for example, the user attempts to insert a string element to a tree whose keys are integers), the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 * <p>
	 * The provided extensions must be used souly by this tree. If an extension was used in another tree, it should not
	 * be passed to a new one for reuse.
	 *
	 * @param  extensions               a collections of extensions to be used by this red black tree.
	 * @throws IllegalArgumentException if the extensions collection is empty
	 */
	public RedBlackTreeExtended(Collection<? extends RedBlackTreeExtension<K, V>> extensions) {
		this(null, extensions);
	}

	/**
	 * Constructs a new, empty red black tree, with the given extensions, with keys ordered according to the specified
	 * comparator.
	 * <p>
	 * All keys inserted into the tree must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(k1, k2)} must not throw a {@code ClassCastException} for any keys {@code k1} and
	 * {@code k2} in the tree. If the user attempts to insert a key to the tree that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 * <p>
	 * The provided extensions must be used souly by this tree. If an extension was used in another tree, it should not
	 * be passed to a new one for reuse.
	 *
	 * @param  comparator               the comparator that will be used to order this tree. If {@code null}, the
	 *                                      {@linkplain Comparable natural ordering} of the keys will be used.
	 * @param  extensions               a collections of extensions to be used by this red black tree.
	 * @throws IllegalArgumentException if the extensions collection is empty
	 */
	@SuppressWarnings("unchecked")
	public RedBlackTreeExtended(Comparator<? super K> comparator,
			Collection<? extends RedBlackTreeExtension<K, V>> extensions) {
		super(comparator);
		if (extensions.isEmpty())
			throw new IllegalArgumentException("No extensions provided. Use the regular Red Black tree.");
		this.extensions = extensions.toArray(len -> new RedBlackTreeExtension[len]);
		for (RedBlackTreeExtension<K, V> extension : extensions)
			Objects.requireNonNull(extension);
		nodes = EmptyNodesArray;
	}

	@Override
	Node<K, V> newNode(K key) {
		int idx = size();
		if (idx >= nodes.length) {
			int newLen = Math.max(2, nodes.length * 2);
			nodes = Arrays.copyOf(nodes, newLen);
			for (RedBlackTreeExtension<K, V> extension : extensions)
				extension.data.expand(newLen);
		}
		assert nodes[idx] == null;
		Node<K, V> n = nodes[idx] = new Node<>(key, idx);
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.initNode(n);
		return n;
	}

	@Override
	void removeNode(RedBlackTree.Node<K, V> n0) {
		super.removeNode(n0);
		Node<K, V> n = (Node<K, V>) n0;
		int nIdx = n.idx;
		assert nodes[nIdx] == n;

		int lastIdx = size();
		Node<K, V> last = nodes[lastIdx];
		assert last.idx == lastIdx;
		nodes[nIdx] = last;

		nodes[lastIdx] = null;

		for (RedBlackTreeExtension<K, V> extension : extensions) {
			extension.data.swap(nIdx, lastIdx);
			extension.data.clear(lastIdx);
		}
		last.idx = nIdx;
		n.idx = -1;
	}

	@Override
	void swap(RedBlackTree.Node<K, V> a, RedBlackTree.Node<K, V> b) {
		Node<K, V> n1 = (Node<K, V>) a, n2 = (Node<K, V>) b;
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.beforeNodeSwap((Node<K, V>) a, (Node<K, V>) b);
		super.swap(n1, n2);
	}

	@Override
	void afterInsert(RedBlackTree.Node<K, V> n) {
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.afterInsert((Node<K, V>) n);
	}

	@Override
	void beforeRemove(RedBlackTree.Node<K, V> n) {
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.beforeRemove((Node<K, V>) n);
	}

	@Override
	void beforeRotateLeft(RedBlackTree.Node<K, V> n) {
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.beforeRotateLeft((Node<K, V>) n);
	}

	@Override
	void beforeRotateRight(RedBlackTree.Node<K, V> n) {
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.beforeRotateRight((Node<K, V>) n);
	}

	void beforeNodeReuse(Node<K, V> node) {
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.initNode(node);
	}

	@Override
	public void clear() {
		int s = size();
		for (RedBlackTreeExtension<K, V> extension : extensions)
			extension.data.clear(s);
		super.clear();
	}

	static class Node<K, V> extends RedBlackTree.Node<K, V> {

		int idx;

		Node(K key, int idx) {
			super(key);
			this.idx = idx;
		}

		Node<K, V> parent() {
			return (Node<K, V>) parent;
		}

		Node<K, V> left() {
			return (Node<K, V>) left;
		}

		Node<K, V> right() {
			return (Node<K, V>) right;
		}

	}

}

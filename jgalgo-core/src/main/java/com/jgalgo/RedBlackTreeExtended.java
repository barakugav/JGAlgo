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
 * RedBlackTreeExtension.Size<Integer> sizeExt = new RedBlackTreeExtension.Size<>();
 * RedBlackTreeExtension.Max<Integer> maxExt = new RedBlackTreeExtension.Max<>();
 * RedBlackTreeExtended<Integer> tree = new RedBlackTreeExtended<>(List.of(sizeExt, maxExt));
 *
 * HeapReference<Integer> e1 = tree.insert(15);
 * tree.insert(5);
 * tree.insert(3);
 * tree.insert(1);
 * ...
 * tree.insert(1);
 *
 * int subTreeSize = sizeExt.getSubTreeSize(e1);
 * HeapReference<Integer> subTreeMax = maxExt.getSubTreeMax(e1);
 * System.out.println("The subtree of " + e1 + " is of size " + subTreeSize);
 * System.out.println("The maximum element in the sub tree of " + e1 + " is " + subTreeMax);
 * }</pre>
 *
 * @author Barak Ugav
 */
public class RedBlackTreeExtended<E> extends RedBlackTree<E> {

	private Node<E>[] nodes;
	private final RedBlackTreeExtension<E>[] extensions;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodesArray = new Node[0];

	/**
	 * Constructs a new, empty red black tree, with the given extensions, sorted according to the natural ordering of
	 * its elements.
	 * <p>
	 * All elements inserted into the tree must implement the {@link Comparable} interface. Furthermore, all such
	 * elements must be <i>mutually comparable</i>: {@code e1.compareTo(e2)} must not throw a {@code ClassCastException}
	 * for any elements {@code e1} and {@code e2} in the tree. If the user attempts to insert an element to the tree
	 * that violates this constraint (for example, the user attempts to insert a string element to a tree whose elements
	 * are integers), the {@code insert} call will throw a {@code ClassCastException}.
	 * <p>
	 * The provided extensions must be used souly by this tree. If an extension was used in another tree, it should not
	 * be passed to a new one for reuse.
	 *
	 * @param  extensions               a collections of extensions to be used by this red black tree.
	 * @throws IllegalArgumentException if the extensions collection is empty
	 */
	public RedBlackTreeExtended(Collection<? extends RedBlackTreeExtension<E>> extensions) {
		this(null, extensions);
	}

	/**
	 * Constructs a new, empty red black tree, with the given extensions, sorted according to the specified comparator.
	 * <p>
	 * All elements inserted into the tree must be <i>mutually comparable</i> by the specified comparator:
	 * {@code comparator.compare(e1, e2)} must not throw a {@code ClassCastException} for any elements {@code e1} and
	 * {@code e2} in the tree. If the user attempts to insert an element to the tree that violates this constraint, the
	 * {@code insert} call will throw a {@code ClassCastException}.
	 * <p>
	 * The provided extensions must be used souly by this tree. If an extension was used in another tree, it should not
	 * be passed to a new one for reuse.
	 *
	 * @param  comparator               the comparator that will be used to order this tree. If {@code null}, the
	 *                                      {@linkplain Comparable natural ordering} of the elements will be used.
	 * @param  extensions               a collections of extensions to be used by this red black tree.
	 * @throws IllegalArgumentException if the extensions collection is empty
	 */
	@SuppressWarnings("unchecked")
	public RedBlackTreeExtended(Comparator<? super E> comparator,
			Collection<? extends RedBlackTreeExtension<E>> extensions) {
		super(comparator);
		if (extensions.isEmpty())
			throw new IllegalArgumentException("No extensions provided. Use the regular Red Black tree.");
		this.extensions = extensions.toArray(len -> new RedBlackTreeExtension[len]);
		for (RedBlackTreeExtension<E> extension : extensions)
			Objects.requireNonNull(extension);
		nodes = EmptyNodesArray;
	}

	@Override
	Node<E> newNode(E e) {
		int idx = size();
		if (idx >= nodes.length) {
			int newLen = Math.max(2, nodes.length * 2);
			nodes = Arrays.copyOf(nodes, newLen);
			for (RedBlackTreeExtension<E> extension : extensions)
				extension.data.expand(newLen);
		}
		assert nodes[idx] == null;
		Node<E> n = nodes[idx] = new Node<>(e, idx);
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.initNode(n);
		return n;
	}

	@Override
	void removeNode(RedBlackTree.Node<E> n0) {
		super.removeNode(n0);
		Node<E> n = (Node<E>) n0;
		int nIdx = n.idx;
		assert nodes[nIdx] == n;

		int lastIdx = size();
		Node<E> last = nodes[lastIdx];
		assert last.idx == lastIdx;
		nodes[nIdx] = last;

		nodes[lastIdx] = null;

		for (RedBlackTreeExtension<E> extension : extensions) {
			extension.data.swap(nIdx, lastIdx);
			extension.data.clear(lastIdx);
		}
		last.idx = nIdx;
		n.idx = -1;
	}

	@Override
	void swap(RedBlackTree.Node<E> a, RedBlackTree.Node<E> b) {
		Node<E> n1 = (Node<E>) a, n2 = (Node<E>) b;
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.beforeNodeSwap((Node<E>) a, (Node<E>) b);
		super.swap(n1, n2);
	}

	@Override
	void afterInsert(RedBlackTree.Node<E> n) {
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.afterInsert((Node<E>) n);
	}

	@Override
	void beforeRemove(RedBlackTree.Node<E> n) {
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.beforeRemove((Node<E>) n);
	}

	@Override
	void beforeRotateLeft(RedBlackTree.Node<E> n) {
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.beforeRotateLeft((Node<E>) n);
	}

	@Override
	void beforeRotateRight(RedBlackTree.Node<E> n) {
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.beforeRotateRight((Node<E>) n);
	}

	void beforeNodeReuse(Node<E> node) {
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.initNode(node);
	}


	@Override
	public void clear() {
		int s = size();
		for (RedBlackTreeExtension<E> extension : extensions)
			extension.data.clear(s);
		super.clear();
	}

	static class Node<E> extends RedBlackTree.Node<E> {

		int idx;

		Node(E e, int idx) {
			super(e);
			this.idx = idx;
		}

		Node<E> parent() {
			return (Node<E>) parent;
		}

		Node<E> left() {
			return (Node<E>) left;
		}

		Node<E> right() {
			return (Node<E>) right;
		}

	}

}

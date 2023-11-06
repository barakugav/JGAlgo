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

package com.jgalgo.internal.ds;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;

class BinarySearchTrees {

	private BinarySearchTrees() {}

	static <K, NodeT extends Node<K, NodeT>> NodeT find(NodeT root, Comparator<? super K> c, K key) {
		return findOrNeighbor(root, c, key, NeighborType.None);
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findOrSmaller(NodeT root, Comparator<? super K> c, K key) {
		return findOrNeighbor(root, c, key, NeighborType.Predecessor);
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findOrGreater(NodeT root, Comparator<? super K> c, K key) {
		return findOrNeighbor(root, c, key, NeighborType.Successor);
	}

	private static enum NeighborType {
		None, Predecessor, Successor,
	}

	private static <K, NodeT extends Node<K, NodeT>> NodeT findOrNeighbor(NodeT root, Comparator<? super K> c, K key,
			NeighborType neighborType) {
		if (root == null)
			return null;
		BiFunction<NeighborType, NodeT, NodeT> onLeftChildMissing = (nType, p) -> {
			switch (nType) {
				case None:
					return null;
				case Predecessor:
					return getPredecessor(p);
				case Successor:
					return p;
				default:
					throw new IllegalArgumentException("Unexpected value: " + neighborType);
			}
		};
		BiFunction<NeighborType, NodeT, NodeT> onRightChildMissing = (nType, p) -> {
			switch (nType) {
				case None:
					return null;
				case Predecessor:
					return p;
				case Successor:
					return getSuccessor(p);
				default:
					throw new IllegalArgumentException("Unexpected value: " + neighborType);
			}
		};
		if (c == null) {
			for (NodeT p = root;;) {
				int cmp = JGAlgoUtils.cmpDefault(key, p.key);
				if (cmp < 0) {
					if (!p.hasLeftChild())
						return onLeftChildMissing.apply(neighborType, p);
					p = p.left;
				} else if (cmp > 0) {
					if (!p.hasRightChild())
						return onRightChildMissing.apply(neighborType, p);
					p = p.right;
				} else {
					return p;
				}
			}
		} else {
			for (NodeT p = root;;) {
				int cmp = c.compare(key, p.key);
				if (cmp < 0) {
					if (!p.hasLeftChild())
						return onLeftChildMissing.apply(neighborType, p);
					p = p.left;
				} else if (cmp > 0) {
					if (!p.hasRightChild())
						return onRightChildMissing.apply(neighborType, p);
					p = p.right;
				} else {
					return p;
				}
			}
		}
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findSmaller(NodeT root, Comparator<? super K> c, K key) {
		if (root == null)
			return null;
		if (c == null) {
			for (NodeT p = root;;) {
				int cmp = JGAlgoUtils.cmpDefault(key, p.key);
				if (cmp <= 0) {
					if (!p.hasLeftChild())
						return getPredecessor(p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return p;
					p = p.right;
				}
			}
		} else {
			for (NodeT p = root;;) {
				int cmp = c.compare(key, p.key);
				if (cmp <= 0) {
					if (!p.hasLeftChild())
						return getPredecessor(p);
					p = p.left;
				} else {
					if (!p.hasRightChild())
						return p;
					p = p.right;
				}
			}
		}
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findGreater(NodeT root, Comparator<? super K> c, K key) {
		if (root == null)
			return null;
		if (c == null) {
			for (NodeT p = root;;) {
				int cmp = JGAlgoUtils.cmpDefault(key, p.key);
				if (cmp >= 0) {
					if (!p.hasRightChild())
						return getSuccessor(p);
					p = p.right;
				} else {
					if (!p.hasLeftChild())
						return p;
					p = p.left;
				}
			}
		} else {
			for (NodeT p = root;;) {
				int cmp = c.compare(key, p.key);
				if (cmp >= 0) {
					if (!p.hasRightChild())
						return getSuccessor(p);
					p = p.right;
				} else {
					if (!p.hasLeftChild())
						return p;
					p = p.left;
				}
			}
		}
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findMin(NodeT root) {
		for (NodeT p = root;; p = p.left)
			if (!p.hasLeftChild())
				return p;
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT findMax(NodeT root) {
		for (NodeT p = root;; p = p.right)
			if (!p.hasRightChild())
				return p;
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT getPredecessor(NodeT n) {
		return getPredecessorInSubtree(n, null);
	}

	private static <K, NodeT extends Node<K, NodeT>> NodeT getPredecessorInSubtree(NodeT n, NodeT subtreeRoot) {
		/* predecessor in left sub tree */
		if (n.hasLeftChild())
			for (NodeT p = n.left;; p = p.right)
				if (!p.hasRightChild())
					return p;

		/* predecessor is some ancestor */
		NodeT subtreeParent = subtreeRoot != null ? subtreeRoot.parent : null;
		for (NodeT p = n; p.parent != subtreeParent; p = p.parent)
			if (p.isRightChild())
				return p.parent;
		return null;
	}

	static <K, NodeT extends Node<K, NodeT>> NodeT getSuccessor(NodeT n) {
		return getSuccessorInSubtree(n, null);
	}

	private static <K, NodeT extends Node<K, NodeT>> NodeT getSuccessorInSubtree(NodeT n, NodeT subtreeRoot) {
		/* successor in right sub tree */
		if (n.hasRightChild())
			for (NodeT p = n.right;; p = p.left)
				if (!p.hasLeftChild())
					return p;

		/* successor is some ancestor */
		NodeT subtreeParent = subtreeRoot != null ? subtreeRoot.parent : null;
		for (NodeT p = n; p.parent != subtreeParent; p = p.parent)
			if (p.isLeftChild())
				return p.parent;
		return null;
	}

	static <K, NodeT extends Node<K, NodeT>> void insert(NodeT root, Comparator<? super K> c, NodeT n) {
		if (c == null) {
			for (NodeT parent = root;;) {
				int cmp = JGAlgoUtils.cmpDefault(n.key, parent.key);
				if (cmp <= 0) {
					if (!parent.hasLeftChild()) {
						parent.left = n;
						n.parent = parent;
						return;
					}
					parent = parent.left;
				} else {
					if (!parent.hasRightChild()) {
						parent.right = n;
						n.parent = parent;
						return;
					}
					parent = parent.right;
				}
			}
		} else {
			for (NodeT parent = root;;) {
				int cmp = c.compare(n.key, parent.key);
				if (cmp <= 0) {
					if (!parent.hasLeftChild()) {
						parent.left = n;
						n.parent = parent;
						return;
					}
					parent = parent.left;
				} else {
					if (!parent.hasRightChild()) {
						parent.right = n;
						n.parent = parent;
						return;
					}
					parent = parent.right;
				}
			}
		}
	}

	static <K, NodeT extends Node<K, NodeT>> void clear(NodeT root) {
		for (NodeT p = root; p != null;) {
			for (;;) {
				if (p.hasLeftChild()) {
					p = p.left;
					continue;
				}
				if (p.hasRightChild()) {
					p = p.right;
					continue;
				}
				break;
			}
			NodeT parent = p.parent;
			p.clear();
			p = parent;
		}
	}

	static <K, NodeT extends Node<K, NodeT>> void swap(NodeT n1, NodeT n2) {
		if (n2 == n1.parent) {
			NodeT temp = n1;
			n1 = n2;
			n2 = temp;
		}
		if (n1 == n2.parent) {
			if (n1.isLeftChild()) {
				n1.parent.left = n2;
			} else if (n1.isRightChild()) {
				n1.parent.right = n2;
			}
			if (n1.left == n2) {
				NodeT right = n1.right;
				if ((n1.left = n2.left) != null)
					n1.left.parent = n1;
				if ((n1.right = n2.right) != null)
					n1.right.parent = n1;
				n2.left = n1;
				if ((n2.right = right) != null)
					n2.right.parent = n2;
			} else {
				assert n1.right == n2;
				NodeT left = n1.left;
				if ((n1.left = n2.left) != null)
					n1.left.parent = n1;
				if ((n1.right = n2.right) != null)
					n1.right.parent = n1;
				if ((n2.left = left) != null)
					n2.left.parent = n2;
				n2.right = n1;
			}
			n2.parent = n1.parent;
			n1.parent = n2;

		} else {
			if (n1.isLeftChild()) {
				n1.parent.left = n2;
			} else if (n1.isRightChild()) {
				n1.parent.right = n2;
			}
			if (n2.isLeftChild()) {
				n2.parent.left = n1;
			} else if (n2.isRightChild()) {
				n2.parent.right = n1;
			}

			NodeT parent = n1.parent;
			NodeT left = n1.left;
			NodeT right = n1.right;
			n1.parent = n2.parent;
			if ((n1.left = n2.left) != null)
				n1.left.parent = n1;
			if ((n1.right = n2.right) != null)
				n1.right.parent = n1;
			n2.parent = parent;
			if ((n2.left = left) != null)
				n2.left.parent = n2;
			if ((n2.right = right) != null)
				n2.right.parent = n2;
		}
	}

	static class Node<K, NodeT extends Node<K, NodeT>> {
		K key;
		NodeT parent;
		NodeT right;
		NodeT left;

		Node(K key) {
			this.key = key;
			parent = right = left = null;
		}

		void clear() {
			clearWithoutUserData();
			key = null;
		}

		void clearWithoutUserData() {
			parent = left = right = null;
		}

		@Override
		public String toString() {
			return "<" + key + ">";
		}

		boolean isRoot() {
			return parent == null;
		}

		boolean isLeftChild() {
			return !isRoot() && this == parent.left;
		}

		boolean isRightChild() {
			return !isRoot() && this == parent.right;
		}

		boolean hasLeftChild() {
			return left != null;
		}

		boolean hasRightChild() {
			return right != null;
		}
	}

	static class BSTIterator<K, NodeT extends Node<K, NodeT>> implements Iterator<NodeT> {

		private final NodeT subtreeRoot;
		private NodeT n;

		BSTIterator(NodeT subtreeRoot) {
			this.subtreeRoot = subtreeRoot;
			n = subtreeRoot == null ? null : findMin(subtreeRoot);
		}

		@Override
		public boolean hasNext() {
			return n != null;
		}

		@Override
		public NodeT next() {
			Assertions.Iters.hasNext(this);
			NodeT ret = n;
			n = getSuccessorInSubtree(n, subtreeRoot);
			return ret;
		}

	}

}

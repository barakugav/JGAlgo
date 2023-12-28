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

import java.util.Iterator;
import com.jgalgo.internal.util.Assertions;

class BinarySearchTrees {

	private BinarySearchTrees() {}

	static enum NeighborType {
		None {
			@Override
			<NodeT extends Node<NodeT>> NodeT onLeftChildMissing(NodeT p) {
				return null;
			}

			@Override
			<NodeT extends Node<NodeT>> NodeT onRightChildMissing(NodeT p) {
				return null;
			}
		},
		Predecessor {
			@Override
			<NodeT extends Node<NodeT>> NodeT onLeftChildMissing(NodeT p) {
				return BinarySearchTrees.getPredecessor(p);
			}

			@Override
			<NodeT extends Node<NodeT>> NodeT onRightChildMissing(NodeT p) {
				return p;
			}
		},
		Successor {
			@Override
			<NodeT extends Node<NodeT>> NodeT onLeftChildMissing(NodeT p) {
				return p;
			}

			@Override
			<NodeT extends Node<NodeT>> NodeT onRightChildMissing(NodeT p) {
				return BinarySearchTrees.getSuccessor(p);
			}
		};

		abstract <NodeT extends Node<NodeT>> NodeT onLeftChildMissing(NodeT p);

		abstract <NodeT extends Node<NodeT>> NodeT onRightChildMissing(NodeT p);
	}

	static <NodeT extends Node<NodeT>> NodeT findMin(NodeT root) {
		for (NodeT p = root;; p = p.left)
			if (!p.hasLeftChild())
				return p;
	}

	static <NodeT extends Node<NodeT>> NodeT findMax(NodeT root) {
		for (NodeT p = root;; p = p.right)
			if (!p.hasRightChild())
				return p;
	}

	static <NodeT extends Node<NodeT>> NodeT getPredecessor(NodeT n) {
		return getPredecessorInSubtree(n, null);
	}

	private static <NodeT extends Node<NodeT>> NodeT getPredecessorInSubtree(NodeT n, NodeT subtreeRoot) {
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

	static <NodeT extends Node<NodeT>> NodeT getSuccessor(NodeT n) {
		return getSuccessorInSubtree(n, null);
	}

	private static <NodeT extends Node<NodeT>> NodeT getSuccessorInSubtree(NodeT n, NodeT subtreeRoot) {
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

	static <NodeT extends Node<NodeT>> void clear(NodeT root) {
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

	static <NodeT extends Node<NodeT>> void swap(NodeT n1, NodeT n2) {
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

	static class Node<NodeT extends Node<NodeT>> {
		NodeT parent;
		NodeT right;
		NodeT left;

		void clear() {
			clearWithoutUserData();
		}

		void clearWithoutUserData() {
			parent = left = right = null;
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

	static class BSTIterator<NodeT extends Node<NodeT>> implements Iterator<NodeT> {

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

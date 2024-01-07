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
import java.util.function.Consumer;
import com.jgalgo.internal.util.Assertions;

class Trees {

	private Trees() {}

	interface TreeNode<NodeT extends TreeNode<NodeT>> {

		NodeT parent();

		NodeT next();

		NodeT prev();

		NodeT child();

		void setParent(NodeT x);

		void setNext(NodeT x);

		void setPrev(NodeT x);

		void setChild(NodeT x);

	}

	static class TreeNodeImpl<NodeT extends TreeNodeImpl<NodeT>> implements TreeNode<NodeT> {

		NodeT parent;
		NodeT next;
		NodeT prev;
		NodeT child;

		@Override
		public NodeT parent() {
			return parent;
		}

		@Override
		public NodeT next() {
			return next;
		}

		@Override
		public NodeT prev() {
			return prev;
		}

		@Override
		public NodeT child() {
			return child;
		}

		@Override
		public void setParent(NodeT x) {
			parent = x;
		}

		@Override
		public void setNext(NodeT x) {
			next = x;
		}

		@Override
		public void setPrev(NodeT x) {
			prev = x;
		}

		@Override
		public void setChild(NodeT x) {
			child = x;
		}
	}

	static <NodeT extends TreeNode<NodeT>> void clear(NodeT root, Consumer<? super NodeT> finalizer) {
		for (NodeT p = root;;) {
			while (p.child() != null) {
				p = p.child();
				while (p.next() != null)
					p = p.next();
			}

			NodeT prev;
			if (p.prev() != null) {
				prev = p.prev();
				prev.setNext(null);
				p.setPrev(null);
			} else if (p.parent() != null) {
				prev = p.parent();
				prev.setChild(null);
				p.setParent(null);
			} else {
				prev = null;
			}

			finalizer.accept(p);
			if (prev == null)
				break;
			p = prev;
		}
	}

	static class PreOrderIter<NodeT extends TreeNode<NodeT>> implements Iterator<NodeT> {

		private NodeT p;

		PreOrderIter(NodeT p) {
			reset(p);
		}

		void reset(NodeT p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		boolean advance() {
			NodeT next;
			if ((next = p.child()) != null) {
				p = next;
				return true;
			} else {
				NodeT p0 = p;
				do {
					if ((next = p0.next()) != null) {
						p = next;
						return true;
					}
				} while ((p0 = p0.parent()) != null);
				p = null;
				return false;
			}
		}

		@Override
		public NodeT next() {
			Assertions.hasNext(this);
			NodeT ret = p;
			advance();
			return ret;
		}

	}

	// static class PostOrderIter<N extends TreeNode<N>> implements Iterator<N> {

	// private N p;

	// PostOrderIter(N p) {
	// reset(p);
	// }

	// void reset(N p) {
	// for (N next; (next = p.child()) != null;)
	// p = next;
	// this.p = p;
	// }

	// @Override
	// public boolean hasNext() {
	// return p != null;
	// }

	// @Override
	// public N next() {
	// Assertions.hasNext(this);
	// final N ret = p;

	// N next;
	// if ((next = ret.next()) != null) {
	// /* lower child */
	// for (N child; (child = next.child()) != null;)
	// next = child;
	// p = next;
	// } else {
	// p = ret.parent();
	// }

	// return ret;
	// }

	// }

}

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
package com.jgalgo.internal.data;

import java.util.Iterator;
import java.util.function.Consumer;
import com.jgalgo.internal.util.Assertions;

class Trees {

	private Trees() {}

	interface TreeNode<Node extends TreeNode<Node>> {

		Node parent();

		Node next();

		Node prev();

		Node child();

		void setParent(Node x);

		void setNext(Node x);

		void setPrev(Node x);

		void setChild(Node x);

	}

	static class TreeNodeImpl<Node extends TreeNodeImpl<Node>> implements TreeNode<Node> {

		Node parent;
		Node next;
		Node prev;
		Node child;

		@Override
		public Node parent() {
			return parent;
		}

		@Override
		public Node next() {
			return next;
		}

		@Override
		public Node prev() {
			return prev;
		}

		@Override
		public Node child() {
			return child;
		}

		@Override
		public void setParent(Node x) {
			parent = x;
		}

		@Override
		public void setNext(Node x) {
			next = x;
		}

		@Override
		public void setPrev(Node x) {
			prev = x;
		}

		@Override
		public void setChild(Node x) {
			child = x;
		}
	}

	static <Node extends TreeNode<Node>> void clear(Node root, Consumer<? super Node> finalizer) {
		for (Node p = root;;) {
			while (p.child() != null) {
				p = p.child();
				while (p.next() != null)
					p = p.next();
			}

			Node prev;
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

	static class PreOrderIter<Node extends TreeNode<Node>> implements Iterator<Node> {

		private Node p;

		PreOrderIter(Node p) {
			reset(p);
		}

		void reset(Node p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public Node next() {
			Assertions.Iters.hasNext(this);
			final Node ret = p;

			Node next;
			if ((next = ret.child()) != null) {
				p = next;
			} else {
				Node p0 = ret;
				do {
					if ((next = p0.next()) != null) {
						p0 = next;
						break;
					}
				} while ((p0 = p0.parent()) != null);
				p = p0;
			}

			return ret;
		}

	}

	static class PostOrderIter<N extends TreeNode<N>> implements Iterator<N> {

		private N p;

		PostOrderIter(N p) {
			reset(p);
		}

		void reset(N p) {
			for (N next; (next = p.child()) != null;)
				p = next;
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public N next() {
			Assertions.Iters.hasNext(this);
			final N ret = p;

			N next;
			if ((next = ret.next()) != null) {
				/* lower child */
				for (N child; (child = next.child()) != null;)
					next = child;
				p = next;
			} else {
				p = ret.parent();
			}

			return ret;
		}

	}

}

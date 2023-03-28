package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

class Trees {

	private Trees() {
	}

	interface TreeNode<N extends TreeNode<N>> {

		N parent();

		N next();

		N prev();

		N child();

		void setParent(N x);

		void setNext(N x);

		void setPrev(N x);

		void setChild(N x);

	}

	static <N extends TreeNode<N>> void clear(N root, Consumer<? super N> finalizer) {
		List<N> stack = new ArrayList<>();

		stack.add(root);

		do {
			int idx = stack.size() - 1;
			N n = stack.get(idx);
			stack.remove(idx);

			for (N p = n.child(); p != null; p = p.next())
				stack.add(p);

			n.setParent(null);
			n.setNext(null);
			n.setPrev(null);
			n.setChild(null);
			finalizer.accept(n);
		} while (!stack.isEmpty());
	}

	static class PreOrderIter<N extends TreeNode<N>> implements Iterator<N> {

		private N p;

		PreOrderIter(N p) {
			reset(p);
		}

		void reset(N p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public N next() {
			if (!hasNext())
				throw new NoSuchElementException();
			final N ret = p;

			N next;
			if ((next = ret.child()) != null) {
				p = next;
			} else {
				N p0 = ret;
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
			if (!hasNext())
				throw new NoSuchElementException();
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

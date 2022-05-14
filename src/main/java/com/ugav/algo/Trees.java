package com.ugav.algo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

class Trees {

	private Trees() {
		throw new InternalError();
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

	static class Iter<N extends TreeNode<N>> implements Iterator<N> {

		N p;
		boolean valid;

		Iter(N p) {
			reset0(p);
		}

		private void reset0(N p) {
			this.p = p;
			valid = p != null;
		}

		public void reset(N p) {
			reset0(p);
		}

		public boolean hasNext0() {
			if (p == null)
				return false;
			N q;

			if ((q = p.child()) != null) {
				p = q;
				return true;
			}
			do {
				if ((q = p.next()) != null) {
					p = q;
					return true;
				}
			} while ((p = p.parent()) != null);

			return p != null;
		}

		@Override
		public boolean hasNext() {
			if (valid)
				return true;
			return valid = hasNext0();
		}

		@Override
		public N next() {
			if (!hasNext())
				throw new NoSuchElementException();
			valid = false;
			return p;
		}

	}

}

package com.ugav.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

class Trees {

	private Trees() {
		throw new InternalError();
	}

	interface TreeNode {

		TreeNode parent();

		TreeNode next();

		TreeNode prev();

		TreeNode child();

		void setParent(TreeNode x);

		void setNext(TreeNode x);

		void setPrev(TreeNode x);

		void setChild(TreeNode x);

	}

	static void clear(TreeNode root, Consumer<? super TreeNode> finalizer) {
		List<TreeNode> stack = new ArrayList<>();

		stack.add(root);

		do {
			int idx = stack.size() - 1;
			TreeNode n = stack.get(idx);
			stack.remove(idx);

			for (TreeNode p = n.child(); p != null; p = p.next())
				stack.add(p);

			n.setParent(null);
			n.setNext(null);
			n.setPrev(null);
			n.setChild(null);
			finalizer.accept(n);
		} while (!stack.isEmpty());
	}

	static class Iterator {

		TreeNode p;
		boolean valid;

		Iterator(TreeNode p) {
			reset0(p);
		}

		private void reset0(TreeNode p) {
			this.p = p;
			valid = p != null;
		}

		public void reset(TreeNode p) {
			reset0(p);
		}

		public boolean hasNext0() {
			if (p == null)
				return false;
			TreeNode q;

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

		public boolean hasNext() {
			if (valid)
				return true;
			return valid = hasNext0();
		}

		TreeNode nextNode() {
			if (!hasNext())
				throw new NoSuchElementException();
			valid = false;
			return p;
		}

	}

}

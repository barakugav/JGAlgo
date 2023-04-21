package com.jgalgo;

public class DynamicTreeSplaySized extends DynamicTreeSplay {

	public DynamicTreeSplaySized(double weightLimit) {
		super(new SplayImplWithSize(), weightLimit);
	}

	@Override
	public int size(Node v) {
		SplayNodeSized n = (SplayNodeSized) v;
		splay(n);
		return n.size;
	}

	@Override
	SplayNodeSized newNode() {
		return new SplayNodeSized();
	}

	@Override
	void beforeCut(SplayNode n0) {
		super.beforeCut(n0);
		SplayNodeSized n = (SplayNodeSized) n0;
		n.size -= ((SplayNodeSized) n.right).size;
	}

	@Override
	void afterLink(SplayNode n0) {
		super.afterLink(n0);
		SplayNodeSized parent = (SplayNodeSized) n0.userParent;
		parent.size += ((SplayNodeSized) n0).size;
	}

	static class SplayNodeSized extends DynamicTreeSplay.SplayNode {

		int size;

		SplayNodeSized() {
			size = 1;
		}

	}

	static class SplayImplWithSize extends DynamicTreeSplay.SplayImplWithRelativeWeights {

		@Override
		void beforeRotate(SplayNode n0) {
			super.beforeRotate(n0);

			SplayNodeSized n = (SplayNodeSized) n0;
			SplayNodeSized parent = (SplayNodeSized) n.parent;
			int parentOldSize = parent.size;

			if (n.isLeftChild()) {
				parent.size = parentOldSize - n.size + (n.hasRightChild() ? ((SplayNodeSized) n.right).size : 0);
			} else {
				assert n.isRightChild();
				parent.size = parentOldSize - n.size + (n.hasLeftChild() ? ((SplayNodeSized) n.left).size : 0);
			}

			n.size = parentOldSize;
		}

	}

}

package com.ugav.algo;

public class DynamicTreeSplaySized<V, E> extends DynamicTreeSplay<V, E> {

	public DynamicTreeSplaySized(double weightLimit) {
		super(new SplayImplWithSize<>(), weightLimit);
	}

	@Override
	public int size(Node<V, E> v) {
		SplayNodeSized<V, E> n = (SplayNodeSized<V, E>) v;
		splay(n);
		return n.size;
	}

	@Override
	SplayNodeSized<V, E> newNode(V nodeData) {
		return new SplayNodeSized<>(nodeData);
	}

	@Override
	void beforeCut(SplayNode<V, E> n0) {
		super.beforeCut(n0);
		SplayNodeSized<V, E> n = (SplayNodeSized<V, E>) n0;
		n.size -= ((SplayNodeSized<V, E>) n.right).size;
	}

	@Override
	void afterLink(SplayNode<V, E> n0) {
		super.afterLink(n0);
		SplayNodeSized<V, E> parent = (SplayNodeSized<V, E>) n0.userParent;
		parent.size += ((SplayNodeSized<V, E>) n0).size;
	}

	static class SplayNodeSized<V, E> extends DynamicTreeSplay.SplayNode<V, E> {

		int size;

		SplayNodeSized(V nodeData) {
			super(nodeData);
			size = 1;
		}

	}

	static class SplayImplWithSize<V, E> extends DynamicTreeSplay.SplayImplWithRelativeWeights<V, E> {

		@Override
		void beforeRotate(SplayNode<V, E> n0) {
			super.beforeRotate(n0);

			SplayNodeSized<V, E> n = (SplayNodeSized<V, E>) n0;
			SplayNodeSized<V, E> parent = (SplayNodeSized<V, E>) n.parent;
			int parentOldSize = parent.size;

			if (n.isLeftChild()) {
				parent.size = parentOldSize - n.size + (n.hasRightChild() ? ((SplayNodeSized<V, E>) n.right).size : 0);
			} else {
				assert n.isRightChild();
				parent.size = parentOldSize - n.size + (n.hasLeftChild() ? ((SplayNodeSized<V, E>) n.left).size : 0);
			}

			n.size = parentOldSize;
		}

	}

}

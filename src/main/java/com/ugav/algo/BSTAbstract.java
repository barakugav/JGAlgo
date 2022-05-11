package com.ugav.algo;

public abstract class BSTAbstract<E> extends HeapAbstractDirectAccessed<E> implements BST<E> {

	@Override
	public E findMax() {
		return findMaxHandle().get();
	}

	@Override
	public E extractMax() {
		Handle<E> min = findMaxHandle();
		E val = min.get();
		removeHandle(min);
		return val;
	}

}

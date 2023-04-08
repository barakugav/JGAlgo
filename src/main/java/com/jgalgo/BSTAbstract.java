package com.jgalgo;

import java.util.Comparator;

public abstract class BSTAbstract<E> extends HeapAbstractDirectAccessed<E> implements BST<E> {

	public BSTAbstract(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public E findMax() {
		return findMaxHandle().get();
	}

	@Override
	public E extractMax() {
		Handle<E> max = findMaxHandle();
		E val = max.get();
		removeHandle(max);
		return val;
	}

}

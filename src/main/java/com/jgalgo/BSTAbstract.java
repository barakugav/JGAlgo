package com.jgalgo;

import java.util.Comparator;

public abstract class BSTAbstract<E> extends HeapReferenceableAbstract<E> implements BST<E> {

	public BSTAbstract(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public E findMax() {
		return findMaxRef().get();
	}

	@Override
	public E extractMax() {
		HeapReference<E> max = findMaxRef();
		E val = max.get();
		removeRef(max);
		return val;
	}

}

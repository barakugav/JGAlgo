package com.jgalgo;

import java.util.Comparator;
import java.util.Iterator;

abstract class HeapReferenceableAbstract<E> extends HeapAbstract<E> implements HeapReferenceable<E> {

	HeapReferenceableAbstract(Comparator<? super E> c) {
		super(c);
	}

	@Override
	public Iterator<E> iterator() {
		return HeapReferenceable.super.iterator();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return findRef((E) o) != null;
	}

	@Override
	public E findMin() {
		return findMinRef().get();
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		HeapReference<E> ref = findRef((E) o);
		if (ref == null)
			return false;
		removeRef(ref);
		return true;
	}

	@Override
	public E extractMin() {
		HeapReference<E> min = findMinRef();
		E val = min.get();
		removeRef(min);
		return val;
	}

}

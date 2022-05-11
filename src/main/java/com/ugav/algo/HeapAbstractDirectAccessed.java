package com.ugav.algo;

public abstract class HeapAbstractDirectAccessed<E> extends HeapAbstract<E> implements HeapDirectAccessed<E> {

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		return findHanlde((E) o) != null;
	}

	@Override
	public E findMin() {
		return findMinHandle().get();
	}

	@Override
	public boolean remove(Object o) {
		@SuppressWarnings("unchecked")
		Handle<E> handle = findHanlde((E) o);
		if (handle == null)
			return false;
		removeHandle(handle);
		return true;
	}

	@Override
	public E extractMin() {
		Handle<E> min = findMinHandle();
		E val = min.get();
		removeHandle(min);
		return val;
	}

}

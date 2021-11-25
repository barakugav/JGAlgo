package com.ugav.algo;

import java.util.Collection;
import java.util.Iterator;

public interface Heap<E> extends Collection<E> {

	int size();

	default boolean isEmpty() {
		return size() == 0;
	}

	Handle<E> insert(E e);

	boolean remove(Object e);

	E findMin();

	E extractMin();

	Iterator<E> iterator();

	void meld(Heap<? extends E> h);

	boolean isHandlesSupported();

	Handle<E> findHanlde(E e);

	Handle<E> findMinHandle();

	void decreaseKey(Handle<E> handle, E e);

	void removeHandle(Handle<E> handle);

	static interface Handle<E> {

		E get();

	}

}

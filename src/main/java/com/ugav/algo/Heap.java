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

    Handle<E> findMinHandle(Handle<E> h);

    static interface Handle<E> {

	E get();

	void decreaseKey(E e);

	void remove();

    }

}

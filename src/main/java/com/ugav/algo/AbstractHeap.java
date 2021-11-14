package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Collection;

public abstract class AbstractHeap<E> extends AbstractCollection<E> implements Heap<E> {

    @Override
    public boolean removeAll(Collection<?> c) {
	boolean modified = false;
	for (Object e : c)
	    if (remove(e))
		modified = true;
	return modified;
    }

    @Override
    public E extractMin() {
	E min = findMin();
	remove(min);
	return min;
    }

    @Override
    public void meld(Heap<? extends E> h) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHandlesSupported() {
	return false;
    }

    @Override
    public Handle<E> findMinHandle(Handle<E> h) {
	throw new UnsupportedOperationException();
    }

}
